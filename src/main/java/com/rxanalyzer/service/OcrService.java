package com.rxanalyzer.service;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.gax.core.FixedCredentialsProvider;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class OcrService {

    @Value("${google.cloud.vision.credentials-path}")
    private String credentialsPath;

    private ImageAnnotatorClient createVisionClient() throws IOException {
        InputStream credentialsStream = getClass()
                .getClassLoader()
                .getResourceAsStream("google-vision-credentials.json");

        GoogleCredentials credentials = GoogleCredentials
                .fromStream(credentialsStream)
                .createScoped("https://www.googleapis.com/auth/cloud-platform");

        ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();

        return ImageAnnotatorClient.create(settings);
    }

    public String extractText(MultipartFile file) throws IOException {
        String contentType = file.getContentType();

        if (contentType != null && contentType.equals("application/pdf")) {
            return extractTextFromPdf(file);
        } else {
            return extractTextFromImage(file.getBytes());
        }
    }

    private String extractTextFromImage(byte[] imageBytes) throws IOException {
        try (ImageAnnotatorClient client = createVisionClient()) {
            ByteString imgBytes = ByteString.copyFrom(imageBytes);
            Image image = Image.newBuilder().setContent(imgBytes).build();
            Feature feature = Feature.newBuilder()
                    .setType(Feature.Type.TEXT_DETECTION)
                    .build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feature)
                    .setImage(image)
                    .build();

            BatchAnnotateImagesResponse response = client
                    .batchAnnotateImages(List.of(request));

            AnnotateImageResponse imageResponse = response.getResponses(0);
            if (imageResponse.hasError()) {
                throw new IOException("Vision API error: " +
                        imageResponse.getError().getMessage());
            }

            return imageResponse.getFullTextAnnotation().getText();
        }
    }

    private String extractTextFromPdf(MultipartFile file) throws IOException {
        StringBuilder fullText = new StringBuilder();

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFRenderer renderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage image = renderer.renderImageWithDPI(page, 300);
                byte[] imageBytes = bufferedImageToBytes(image);
                fullText.append(extractTextFromImage(imageBytes));
                fullText.append("\n");
            }
        }

        return fullText.toString();
    }

    private byte[] bufferedImageToBytes(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }

}
