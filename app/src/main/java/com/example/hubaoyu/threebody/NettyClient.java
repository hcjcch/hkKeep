package com.example.hubaoyu.threebody;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import rx.functions.Action0;

public class NettyClient implements TimeClientHandler.ReceiveCallback {

    TimeClientHandler.ReceiveCallback receiveCallback;
    private Action0 inactiveCall;
    EventLoopGroup group;

    public NettyClient(Action0 inactiveCall) {
        this.inactiveCall = inactiveCall;
    }

    public void setReceiveCallback(TimeClientHandler.ReceiveCallback receiveCallback) {
        this.receiveCallback = receiveCallback;
        group = new NioEventLoopGroup();
    }

    @Override
    public void onReceiveData(byte[] data) {
        if (receiveCallback != null) {
            receiveCallback.onReceiveData(data);
        }
    }

    TimeClientHandler timeClientHandler;

    public void connect(int port, String host) throws Exception {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            timeClientHandler = new TimeClientHandler(NettyClient.this, new Action0() {
                                @Override
                                public void call() {
                                    inactiveCall.call();
                                }
                            });
                            ch.pipeline()
                                    .addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, ByteBufUtil.writeUtf8(ByteBufAllocator.DEFAULT, MainActivity.flag)))
                                    .addLast(timeClientHandler);
                        }
                    });
            // 发起异步连接操作
            ChannelFuture future = bootstrap.connect(host, port).sync();
            // 等待客户端链路关闭
            future.channel().closeFuture().sync();
        } finally {
            // 优雅退出，释放NIO线程组
            group.shutdownGracefully();
        }
    }

    public void sendData(byte[] data) {
        if (timeClientHandler != null)
            timeClientHandler.channelWrite(data);
    }

    public void disConnect() {
        if (group != null) {
            group.shutdownGracefully();
        }
    }
}
