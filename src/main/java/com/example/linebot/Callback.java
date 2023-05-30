package com.example.linebot;

import com.example.linebot.replier.Follow;
import com.example.linebot.replier.Parrot;
import com.linecorp.bot.client.LineBlobClient;
import com.linecorp.bot.client.MessageContentResponse;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@LineMessageHandler
public class Callback {

    private static final Logger log = LoggerFactory.getLogger(Callback.class);
    private LineBlobClient client;

    @Autowired
    public Callback(LineBlobClient client) {
        this.client = client;
    }

    @EventMapping
    public Message handleFollow(FollowEvent event) {
        Follow follow = new Follow(event);
        return follow.reply();
    }

    @EventMapping
    public Message handleMessage(MessageEvent<TextMessageContent> event) {
        Parrot parrot = new Parrot(event);
        return parrot.reply();
    }

    //画像を受け取った時の処理
    @EventMapping
    public Message handleImageMessage(MessageEvent<ImageMessageContent> event) {
        //画像のIDを取得
        String msgID = event.getMessage().getId();
        Optional<String> opt = Optional.empty();

        try {
            //画像のIDから画像のバイナリデータを取得
            MessageContentResponse resp = client.getMessageContent(msgID).get();
            log.info("get content{}:",resp);

            //画像のバイナリデータを一時ファイルに書き込み
            opt = makeTmpFile(resp, ".jpg");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        //一時ファイルのパスを取得
        String path = opt.orElseGet(() -> "no file");
        UploadAPI uploadAPI = new UploadAPI(path);
        String response = uploadAPI.doPost();
        return new TextMessage(response);
    }

    //画像のバイナリデータを一時ファイルに書き込みするメソッド
    private Optional<String> makeTmpFile(MessageContentResponse resp, String extension) {
        try (InputStream is = resp.getStream()) {
            Path tmpFilePath = Files.createTempFile("linebot", extension);
            Files.copy(is, tmpFilePath, StandardCopyOption.REPLACE_EXISTING);
            return Optional.ofNullable(tmpFilePath.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
