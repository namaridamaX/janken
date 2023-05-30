package com.example.linebot;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import javax.servlet.http.HttpServlet;

public class UploadAPI extends HttpServlet {

    // APIのURL
    String API_URL = "http://｛自分のEC2のパブリックドメイン}/api/upload";
    // アップロードするファイルのパス
    String File_DIR;

    public UploadAPI(String FILE_DIR) {
        this.File_DIR = FILE_DIR;
    }

    // ファイルをアップロードする
    public String doPost(){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        // リクエストヘッダーの設定
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        // リクエストボディの設定
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("file", new FileSystemResource(File_DIR));

        // リクエストエンティティの設定
        HttpEntity<MultiValueMap<String, Object>> request =
                new HttpEntity<MultiValueMap<String, Object>>(map, headers);
        System.out.println("request: " + request);

        // リクエストの送信
        ResponseEntity<String> response =
                restTemplate.exchange(API_URL,
                        HttpMethod.POST,
                        request,
                        String.class);
        System.out.println("response: " + response);

        // レスポンスの取得
        String body = response.getBody();
        System.out.println("body: " + body);

        return body;
    }


}