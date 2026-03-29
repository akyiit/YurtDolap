const admin = require("firebase-admin");
const logger = require("firebase-functions/logger");
const { onDocumentCreated } = require("firebase-functions/v2/firestore");

admin.initializeApp();

exports.sendChatMessageNotification = onDocumentCreated(
  "chats/{chatId}/messages/{messageId}",
  async (event) => {
    const message = event.data ? event.data.data() : null;
    const chatId = event.params.chatId;
    if (!message || !chatId) return;

    const senderId = message.senderId || "";
    const text = typeof message.text === "string" ? message.text : "";

    const chatSnap = await admin.firestore().collection("chats").doc(chatId).get();
    if (!chatSnap.exists) return;

    const chatData = chatSnap.data() || {};
    const participants = Array.isArray(chatData.participants)
      ? chatData.participants
      : [];
    const recipientIds = participants.filter((uid) => uid && uid !== senderId);
    if (recipientIds.length === 0) return;

    const tokenFetches = recipientIds.map(async (uid) => {
      const userSnap = await admin.firestore().collection("users").doc(uid).get();
      if (!userSnap.exists) return null;

      const token = userSnap.get("fcmToken");
      return typeof token === "string" && token.length > 0 ? token : null;
    });

    const tokens = (await Promise.all(tokenFetches)).filter(Boolean);
    if (tokens.length === 0) return;

    const senderSnap = senderId
      ? await admin.firestore().collection("users").doc(senderId).get()
      : null;
    const senderName =
      senderSnap && senderSnap.exists && senderSnap.get("name")
        ? String(senderSnap.get("name"))
        : "Yeni mesaj";

    const payload = {
      tokens,
      notification: {
        title: senderName,
        body: text || "Yeni mesajiniz var"
      },
      data: {
        type: "chat_message",
        chatId,
        senderId
      },
      android: {
        priority: "high",
        notification: {
          channelId: "yurtdolap_fcm_channel"
        }
      }
    };

    const result = await admin.messaging().sendEachForMulticast(payload);
    logger.info("Chat push sent", {
      chatId,
      recipients: tokens.length,
      successCount: result.successCount,
      failureCount: result.failureCount
    });
  }
);
