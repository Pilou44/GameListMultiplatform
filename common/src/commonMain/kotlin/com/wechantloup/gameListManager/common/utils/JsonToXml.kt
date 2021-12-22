/*
    Copyright 2016 Arnaud Guyon

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
package com.wechantloup.gameListManager.common.utils

import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.io.InputStream
import java.io.StringReader
import java.io.StringWriter
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import javax.xml.transform.OutputKeys
import javax.xml.transform.Source
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParserFactory
import org.xmlpull.v1.XmlSerializer

/**
 * Converts JSON to XML
 */
class JsonToXml private constructor(
    jsonObject: JSONObject,
    forcedAttributes: HashSet<String>,
    forcedContent: HashSet<String>
) {
    class Builder(private val mJson: JSONObject) {
//        private var mJson: JSONObject
        private val mForcedAttributes = HashSet<String>()
        private val mForcedContent = HashSet<String>()

//        /**
//         * Constructor
//         * @param jsonObject a JSON object
//         */
//        constructor(jsonObject: JSONObject) {
//            mJson = jsonObject
//        }

//        /**
//         * Constructor
//         * @param inputStream InputStream containing the JSON
//         */
//        constructor(inputStream: InputStream) : this(
//            fr.arnaudguyon.xmltojsonlib.FileReader.readFileFromInputStream(
//                inputStream
//            )
//        ) {
//        }

        /**
         * Constructor
         * @param jsonString String containing the JSON
         */
        constructor(jsonString: String?) : this(JSONObject(jsonString))

        /**
         * Force a TAG to be an attribute of the parent TAG
         * @param path Path for the attribute, using format like "/parentTag/childTag/childTagAttribute"
         * @return the Builder
         */
        fun forceAttribute(path: String): Builder {
            mForcedAttributes.add(path)
            return this
        }

        /**
         * Force a TAG to be the content of its parent TAG
         * @param path Path for the content, using format like "/parentTag/contentTag"
         * @return the Builder
         */
        fun forceContent(path: String): Builder {
            mForcedContent.add(path)
            return this
        }

        /**
         * Creates the JsonToXml object
         * @return a JsonToXml instance
         */
        fun build(): JsonToXml {
            return JsonToXml(mJson, mForcedAttributes, mForcedContent)
        }
    }

    private val mJson: JSONObject
    private val mForcedAttributes: HashSet<String>
    private val mForcedContent: HashSet<String>

    init {
        mJson = jsonObject
        mForcedAttributes = forcedAttributes
        mForcedContent = forcedContent
    }

    /**
     *
     * @return the XML
     */
    override fun toString(): String {
        val rootNode: Node = Node(null, "")
        prepareObject(rootNode, mJson)
        return nodeToXML(rootNode)
    }
    /**
     *
     * @param indent size of the indent (number of spaces)
     * @return the formatted XML
     */
    /**
     *
     * @return the formatted XML with a default indent (3 spaces)
     */
    @JvmOverloads
    fun toFormattedString(/*@android.support.annotation.IntRange(from = 0) */indent: Int = DEFAULT_INDENTATION): String {
        val input = toString()
        return try {
            val xmlInput: Source = StreamSource(StringReader(input))
            val stringWriter = StringWriter()
            val xmlOutput = StreamResult(stringWriter)
            val transformerFactory = TransformerFactory.newInstance()
            val transformer = transformerFactory.newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "" + indent)
            transformer.transform(xmlInput, xmlOutput)
            xmlOutput.writer.toString()
        } catch (e: Exception) {
            throw RuntimeException(e) // TODO: do my own
        }
    }

    private fun nodeToXML(node: Node): String {
        val serializer: XmlSerializer = XmlPullParserFactory.newInstance().newSerializer()
        val writer = StringWriter()
        return try {
            serializer.setOutput(writer)
            serializer.startDocument("UTF-8", true)
            nodeToXml(serializer, node)
            serializer.endDocument()
            writer.toString()
        } catch (e: IOException) {
            throw RuntimeException(e) // TODO: do my own
        }
    }

    @Throws(IOException::class)
    private fun nodeToXml(serializer: XmlSerializer, node: Node) {
        val nodeName: String = node.getName()
        if (nodeName != null) {
            serializer.startTag("", nodeName)
            for (attribute in node.getAttributes()) {
                serializer.attribute("", attribute.mKey, attribute.mValue)
            }
            val nodeContent: String = node.getContent()
            if (nodeContent != null) {
                serializer.text(nodeContent)
            }
        }
        for (subNode in node.getChildren()) {
            nodeToXml(serializer, subNode)
        }
        if (nodeName != null) {
            serializer.endTag("", nodeName)
        }
    }

    private fun prepareObject(node: Node, json: JSONObject) {
        val keyterator: Iterator<String> = json.keys()
        while (keyterator.hasNext()) {
            val key = keyterator.next()
            val `object`: Any = json.opt(key)
            if (`object` != null) {
                if (`object` is JSONObject) {
                    val subObject: JSONObject = `object` as JSONObject
                    val path: String = node.getPath() + "/" + key
                    val subNode: Node = Node(key, path)
                    node.addChild(subNode)
                    prepareObject(subNode, subObject)
                } else if (`object` is JSONArray) {
                    val array: JSONArray = `object` as JSONArray
                    prepareArray(node, key, array)
                } else {
                    val path: String = node.getPath() + "/" + key
                    // JSON numbers are represented either Integer or Double (IEEE 754)
                    // Long may be represented in scientific notation because they are stored as Double
                    // This workaround attempts to represent Long and Double objects accordingly
                    var value: String?
                    if (`object` is Double) {
                        val d = `object`
                        // If it is a Long
                        if (d % 1 == 0.0) {
                            value = java.lang.Long.toString(d.toLong())
                        } else {
                            // TODO: Set up number of decimal digits per attribute in the builder
                            // Set only once. Represent all double numbers up to 20 decimal digits
                            if (DECIMAL_FORMAT.maximumFractionDigits == 0) {
                                DECIMAL_FORMAT.maximumFractionDigits = 20
                            }
                            value = DECIMAL_FORMAT.format(d)
                        }
                    } else {
                        // Integer, Boolean and String are handled here
                        value = `object`.toString()
                    }
                    if (isAttribute(path)) {
                        node.addAttribute(key, value)
                    } else if (isContent(path)) {
                        node.setContent(value)
                    } else {
                        val subNode: Node = Node(key, node.getPath())
                        subNode.setContent(value)
                        node.addChild(subNode)
                    }
                }
            }
        }
    }

    private fun prepareArray(node: Node, key: String, array: JSONArray) {
        val count: Int = array.length()
        val path: String = node.getPath() + "/" + key
        for (i in 0 until count) {
            val subNode: Node = Node(key, path)
            val `object`: Any = array.opt(i)
            if (`object` != null) {
                if (`object` is JSONObject) {
                    val jsonObject: JSONObject = `object` as JSONObject
                    prepareObject(subNode, jsonObject)
                } else if (`object` is JSONArray) {
                    val subArray: JSONArray = `object` as JSONArray
                    prepareArray(subNode, key, subArray)
                } else {
                    val value = `object`.toString()
                    subNode.name = key
                    subNode.content = value
                }
            }
            node.addChild(subNode)
        }
    }

    private fun isAttribute(path: String): Boolean {
        return mForcedAttributes.contains(path)
    }

    private fun isContent(path: String): Boolean {
        return mForcedContent.contains(path)
    }

    companion object {
        private const val DEFAULT_INDENTATION = 3

        // TODO: Set up Locale in the builder
        private val DECIMAL_FORMAT = DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH))
    }
}