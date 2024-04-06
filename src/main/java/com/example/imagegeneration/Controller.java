package com.example.imagegeneration;


import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.stabilityai.StabilityAiImageClient;
import org.springframework.ai.stabilityai.api.StabilityAiImageOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@RestController
public class Controller {

    final StabilityAiImageClient openaiImageClient;

    public Controller(StabilityAiImageClient openaiImageClient) {
        this.openaiImageClient = openaiImageClient;
    }


    @GetMapping("image")
    public ResponseEntity<byte[]> getImage(@RequestParam String prompt) throws IOException {
        ImageResponse response = openaiImageClient.call(
                new ImagePrompt(prompt,
                        StabilityAiImageOptions.builder()
                                .withStylePreset("cinematic")
                                .withN(4)
                                .withHeight(1024)
                                .withWidth(1024).build())
        );

        byte[] imageData = Base64.decodeBase64(response.getResult().getOutput().getB64Json());
        ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
        BufferedImage image = ImageIO.read(bis);

        // Convert the BufferedImage back to bytes
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bos);
        byte[] imageByteArray = bos.toByteArray();

        // Set the response headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(imageByteArray.length);

        // Return the image bytes as the response body
        return new ResponseEntity<>(imageByteArray, headers, HttpStatus.OK);
    }
}
