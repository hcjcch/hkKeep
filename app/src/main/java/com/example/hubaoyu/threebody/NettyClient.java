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

public class NettyClient implements TimeClientHandler.ReceiveCallback {

    TimeClientHandler.ReceiveCallback receiveCallback;

    public void setReceiveCallback(TimeClientHandler.ReceiveCallback receiveCallback) {
        this.receiveCallback = receiveCallback;
    }

    @Override
    public void onReceiveData(byte[] data) {
        if (receiveCallback != null) {
            receiveCallback.onReceiveData(data);
        }
    }

    TimeClientHandler timeClientHandler;

    public void connect(int port, String host) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            timeClientHandler = new TimeClientHandler(NettyClient.this);
//                            ByteBuf byteBuf = Unpooled.buffer(MainActivity.flagBytes.length);
//                            byteBuf.writeBytes(MainActivity.flagBytes);
//                            ByteBufUtil.writeUtf8(byteBuf, MainActivity.flag);
                            ch.pipeline()
//                                    .addLast(new LineBasedFrameDecoder(Integer.MAX_VALUE))
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
        timeClientHandler.channelWrite(data);
    }
}
