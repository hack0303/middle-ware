package org.cland;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.BufferedSink;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.security.cert.PKIXBuilderParameters;

@Slf4j
public class Main {

    public static void main(String[] args){
        //PKIXBuilderParameters x;
        RequestBody requestBody = new RequestBody() {
            @Nullable
            @Override
            public MediaType contentType() {
                return MediaType.parse("application/json; charset=utf-8");
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                sink.writeUtf8("{}");
            }
        };

        Request request = new Request.Builder()
                .headers(Headers.of("Connection","close"))
                .url("https://www.baidu.com")
                .post(requestBody)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.error("@#>{}", "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                StringBuilder stringBuilder=new StringBuilder();
                stringBuilder.append("\r\n");
                stringBuilder.append( response.protocol() + " " +response.code() + " " + response.message());
                stringBuilder.append("\r\n");
                Headers headers = response.headers();
                for (int i = 0; i < headers.size(); i++) {
                    stringBuilder.append(headers.name(i) + ":" + headers.value(i));
                    stringBuilder.append("\r\n");
                }
                stringBuilder.append(response.body().string());
                log.info("{}", stringBuilder);
            }
        });
    }
}
