package com.example.hubaoyu.threebody;

import android.util.Log;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class TimeClientHandler extends ChannelInboundHandlerAdapter {
    private final ByteBuf message;

    public interface ReceiveCallback {
        void onReceiveData(byte[] data);
    }

    ReceiveCallback receiveCallback;

    ChannelHandlerContext ctx;
    public TimeClientHandler(ReceiveCallback receiveCallback) {
        this.receiveCallback = receiveCallback;
        byte[] req = "QUERY TIME ORDER".getBytes();
        message = Unpooled.buffer(req.length);
        message.writeBytes(req);
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        ctx.writeAndFlush(message);
    }

    public void channelWrite(byte[] data) {
        ByteBuf message = Unpooled.buffer(data.length);
        message.writeBytes(data);
        ctx.writeAndFlush(message);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        if(receiveCallback != null) {
            receiveCallback.onReceiveData(req);
        }
        String body = new String(req, "UTF-8");
        Log.d("kal", "Now is : " + body);
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}