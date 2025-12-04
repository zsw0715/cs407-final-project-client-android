package com.cs407.knot_client_android.ui.chat

import com.google.gson.Gson

// 发送用（支持文本 & 图片等富媒体）
data class MsgSend(
    val type: String = "MSG_SEND",
    val convId: Long,
    val clientMsgId: String,
    val msgType: Int,              // 0 = TEXT, 1 = IMAGE ...
    val contentText: String? = null,
    val mediaUrl: String? = null,
    val mediaThumbUrl: String? = null,
    val mediaMetaJson: String? = null,
    val replyToMsgId: Long? = null,
    val loc: MsgSendLoc? = null
)

data class MsgSendLoc(
    val lat: Double? = null,
    val lng: Double? = null,
    val name: String? = null,
    val accuracy: Float? = null
)

// ACK
data class MsgAck(
    val type: String,
    val msgId: Long,
    val clientMsgId: String,
    val serverTime: Long
)

// 对方新消息
data class MsgNew(
    val type: String,
    val convId: Long,
    val fromUid: Long,
    val msgId: Long,
    val msgType: Int,
    val contentText: String,
    val mediaUrl: String? = null,
    val mediaThumbUrl: String? = null,
    val mediaMetaJson: String? = null
)

// 只为了读 "type"
data class WsEnvelope(val type: String?)

private val gson = Gson()

fun MsgSend.toJson(): String = gson.toJson(this)
fun parseMsgAck(json: String): MsgAck = gson.fromJson(json, MsgAck::class.java)
fun parseMsgNew(json: String): MsgNew = gson.fromJson(json, MsgNew::class.java)
fun extractType(json: String): String? =
    try { gson.fromJson(json, WsEnvelope::class.java).type } catch (_: Exception) { null }
