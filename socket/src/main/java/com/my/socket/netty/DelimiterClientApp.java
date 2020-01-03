package com.my.socket.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Random;

public class DelimiterClientApp {
    private static String separator = "$_";
    private static String str = "Netty is a NIO client server framework which enables quick and easy development of network applications such as protocol servers and clients. It greatly simplifies and streamlines network programming such as TCP and UDP socket server.";
    private static String[] wordArr = str.replace(".", "").split(" ");

    public static void main(String[] args) throws IOException, InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        final ClientHandler clientHandler = new ClientHandler();
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        System.out.println("connect。。。");
                        ByteBuf delimiter = Unpooled.copiedBuffer(separator, Charset.forName("UTF-8"));
                        ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, delimiter));
                        ch.pipeline().addLast(new StringDecoder(Charset.forName("UTF-8")));
                        ch.pipeline().addLast(clientHandler);
                    }
                });
        ChannelFuture f = b.connect("localhost", 19101).sync();
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    System.out.println("Connect done");
                } else {
                    System.out.println("Connect failure");
                }
            }
        });
    }

    public static class ClientHandler extends SimpleChannelInboundHandler {
        private int counter;

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            for (int i = 0; i < 5; i++) {
                String str = String.format("Hello%s%s", i, separator);
                ByteBuf buf = Unpooled.buffer(str.length());
                buf.writeBytes(str.getBytes(Charset.forName("UTF-8")));
                ctx.writeAndFlush(buf);
            }
            System.out.println("channelActive");
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            counter++;
            String str = (String) msg;
            System.out.println(String.format("channelRead0 received %s: %s", counter, str));
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ctx.close();
        }
    }
}
