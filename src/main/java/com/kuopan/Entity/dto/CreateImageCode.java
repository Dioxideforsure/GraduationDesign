package com.kuopan.Entity.dto;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

public class CreateImageCode {
    private int width = 160;
    private int height = 40;
    private int codeCount = 4;
    private int lineCount = 20;
    private String code = null;
    private BufferedImage bufferedImage = null;

    Random r = new Random();

    public CreateImageCode() {
        createImage();
    }

    public CreateImageCode(int width, int height, int codeCount) {
        this.width = width;
        this.height = height;
        this.codeCount = codeCount;
        createImage();
    }

    public CreateImageCode(int width, int height, int codeCount, int lineCount) {
        this.width = width;
        this.height = height;
        this.codeCount = codeCount;
        this.lineCount = lineCount;
        createImage();
    }

    private void createImage() {
        int fontWidth = width / codeCount;
        int fontHeight = height - 5;
        int codeY = height - 8;

        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = bufferedImage.getGraphics();

        graphics.setColor(getRandColor(200, 250));
        graphics.fillRect(0, 0, width, height);

        Font font = new Font("Fixedsys", Font.BOLD, fontHeight);
        graphics.setFont(font);

        for (int i = 0; i < lineCount; i++) {
            int Xs = r.nextInt(width);
            int Ys = r.nextInt(height);
            int Xe = Xs + r.nextInt(width);
            int Ye = Ys + r.nextInt(height);
            graphics.setColor(getRandColor(1, 250));
            graphics.drawLine(Xs, Ys, Xe, Ye);
        }

        float noiseRate = 0.01f;
        int area = (int) (noiseRate * width * height);
        for (int i = 0; i < area; i++) {
            int x = r.nextInt(width);
            int y = r.nextInt(height);
            bufferedImage.setRGB(x, y, r.nextInt(255));
        }

        String strRandom = randomStr(codeCount);
        this.code = strRandom;
        for (int i = 0; i < codeCount; i++) {
            String strRealRandom = strRandom.substring(i, i + 1);
            graphics.setColor(getRandColor(1, 250));

            graphics.drawString(strRealRandom, i * fontWidth + 3, codeY);
        }
    }

    private String randomStr(int n) {
        String str1 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
        String str2 = "";
        int len = str1.length() - 1;
        double r;
        for (int i = 0; i < n; i++) {
            r = (Math.random()) * len;
            str2 = str2 + str1.charAt((int) r);
        }
        return str2;
    }

    private Color getRandColor(int fc, int bc) {
        if (fc > 255) {
            fc = 255;
        }
        if (bc > 255) {
            bc = 255;
        }
        int R = fc + r.nextInt(bc - fc);
        int G = fc + r.nextInt(bc - fc);
        int B = fc + r.nextInt(bc - fc);
        return new Color(R, G, B);
    }

    public void write(OutputStream sos) throws IOException {
        ImageIO.write(bufferedImage, "png", sos);
        sos.close();
    }

    public String getCode() {
        return code;
    }
}
