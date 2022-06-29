package com.kickstarter.libs

import android.content.res.Resources
import android.text.TextUtils
import java.lang.StringBuilder
import java.util.HashMap
import java.util.regex.Matcher
import java.util.regex.Pattern

interface KSString {
    fun format(string: String, key1: String, value1: String?): String
    fun format(string: String, key1: String, value1: String?, key2: String, value2: String?): String
    fun format(
        string: String,
        key1: String,
        value1: String?,
        key2: String,
        value2: String?,
        key3: String,
        value3: String?
    ): String

    fun format(
        string: String,
        key1: String,
        value1: String?,
        key2: String,
        value2: String?,
        key3: String,
        value3: String?,
        key4: String,
        value4: String?
    ): String

    fun format(baseKeyPath: String, count: Int): String
    fun format(
        baseKeyPath: String,
        count: Int,
        key1: String,
        value1: String?
    ): String

    fun format(
        baseKeyPath: String,
        count: Int,
        key1: String,
        value1: String?,
        key2: String,
        value2: String?,
        key3: String,
        value3: String?
    ): String

    fun format(
        baseKeyPath: String,
        count: Int,
        key1: String,
        value1: String?,
        key2: String,
        value2: String?
    ): String
}

class KSStringImpl(private val packageName: String, private val resources: Resources) : KSString {
    /**
     * Replace each key found in the string with its corresponding value.
     */
    override fun format(string: String, key1: String, value1: String?): String {
        val substitutions: Map<String, String?> = HashMap<String, String?>().apply {
            put(key1, value1)
        }
        return replace(string, substitutions)
    }

    /**
     * Replace each key found in the string with its corresponding value.
     */
    override fun format(
        string: String,
        key1: String,
        value1: String?,
        key2: String,
        value2: String?
    ): String {
        val substitutions: Map<String, String?> = HashMap<String, String?>().apply {
            put(key1, value1)
            put(key2, value2)
        }
        return replace(string, substitutions)
    }

    /**
     * Replace each key found in the string with its corresponding value.
     */
    override fun format(
        string: String,
        key1: String,
        value1: String?,
        key2: String,
        value2: String?,
        key3: String,
        value3: String?
    ): String {
        val substitutions: Map<String, String?> = HashMap<String, String?>().apply {
            put(key1, value1)
            put(key2, value2)
            put(key3, value3)
        }
        return replace(string, substitutions)
    }

    /**
     * Replace each key found in the string with its corresponding value.
     */
    override fun format(
        string: String,
        key1: String,
        value1: String?,
        key2: String,
        value2: String?,
        key3: String,
        value3: String?,
        key4: String,
        value4: String?
    ): String {
        val substitutions: Map<String, String?> = HashMap<String, String?>().apply {
            put(key1, value1)
            put(key2, value2)
            put(key3, value3)
            put(key4, value4)
        }
        return replace(string, substitutions)
    }

    /**
     * Given a base key path and count, find the appropriate string resource and replace each key
     * found in the string resource with its corresponding value. For example, given a base key of `foo`,
     * a count of 0 would give the string resource `foo_zero`, a count of 1 `foo_one`, and so on.
     *
     * This particular version is for strings that have no replaceable sections
     */
    override fun format(baseKeyPath: String, count: Int): String {
        return stringFromKeyPath(baseKeyPath, keyPathComponentForCount(count)!!)
    }

    /**
     * Given a base key path and count, find the appropriate string resource and replace each key
     * found in the string resource with its corresponding value. For example, given a base key of `foo`,
     * a count of 0 would give the string resource `foo_zero`, a count of 1 `foo_one`, and so on.
     */
    override fun format(
        baseKeyPath: String,
        count: Int,
        key1: String,
        value1: String?
    ): String {
        val string = stringFromKeyPath(baseKeyPath, keyPathComponentForCount(count)!!)
        return format(string, key1, value1)
    }

    /**
     * Given a base key path and count, find the appropriate string resource and replace each key
     * found in the string resource with its corresponding value. For example, given a base key of `foo`,
     * a count of 0 would give the string resource `foo_zero`, a count of 1 `foo_one`, and so on.
     */
    override fun format(
        baseKeyPath: String,
        count: Int,
        key1: String,
        value1: String?,
        key2: String,
        value2: String?
    ): String {
        val string = stringFromKeyPath(baseKeyPath, keyPathComponentForCount(count)!!)
        return format(string, key1, value1, key2, value2)
    }

    /**
     * Given a base key path and count, find the appropriate string resource and replace each key
     * found in the string resource with its corresponding value. For example, given a base key of `foo`,
     * a count of 0 would give the string resource `foo_zero`, a count of 1 `foo_one`, and so on.
     */
    override fun format(
        baseKeyPath: String,
        count: Int,
        key1: String,
        value1: String?,
        key2: String,
        value2: String?,
        key3: String,
        value3: String?
    ): String {
        val string = stringFromKeyPath(baseKeyPath, keyPathComponentForCount(count)!!)
        return format(string, key1, value1, key2, value2, key3, value3)
    }

    /**
     * Takes a variable length of [String] arguments, joins them together to form a single path, then
     * looks up a string resource given that path. If the resource cannot be found, returns an empty string.
     */
    private fun stringFromKeyPath(vararg keyPathComponents: String): String {
        val keyPath = TextUtils.join("_", keyPathComponents)
        return try {
            val resourceId = resources.getIdentifier(keyPath, "string", packageName)
            resources.getString(resourceId)
        } catch (e: Resources.NotFoundException) {
            ""
        }
    }

    private fun keyPathComponentForCount(count: Int): String? {
        if (count == 0) {
            return "zero"
        } else if (count == 1) {
            return "one"
        } else if (count == 2) {
            return "two"
        } else if (count > 2 && count <= 5) {
            return "few"
        } else if (count > 5) {
            return "many"
        }
        return null
    }

    /**
     * For a given string, replaces occurrences of each key with its corresponding value. In the string, keys are wrapped
     * with `%{}`, e.g. `%{backers_count} backers`. In this instance, the substitutions hash might contain one entry with the key
     * `backers_count` and value `2`.
     */
    private fun replace(string: String, substitutions: Map<String, String?>): String {
        val builder = StringBuilder()
        for (key in substitutions.keys) {
            if (builder.length > 0) {
                builder.append("|")
            }
            builder
                .append("(%\\{")
                .append(key)
                .append("\\})")
        }
        val pattern = Pattern.compile(builder.toString())
        val matcher = pattern.matcher(string)
        val buffer = StringBuffer()
        while (matcher.find()) {
            val key = NON_WORD_REGEXP.matcher(matcher.group()).replaceAll("")
            val value = substitutions[key]
            val replacement = Matcher.quoteReplacement(value ?: "")
            matcher.appendReplacement(buffer, replacement)
        }
        matcher.appendTail(buffer)
        return buffer.toString()
    }

    companion object {
        private val NON_WORD_REGEXP = Pattern.compile("[^\\w]")
    }
}
