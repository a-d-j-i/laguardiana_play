package playrunner;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import play.Logger;
import play.Play;

public class Server {

    public Server() {
        boss = Executors.newCachedThreadPool();
        worker = Executors.newCachedThreadPool();
        channelFactory = new NioServerSocketChannelFactory(boss, worker);
        bootstrap = new ServerBootstrap(channelFactory);
    }

    public class SslPipelineFactory extends play.server.ssl.SslHttpServerPipelineFactory {

        @Override
        public ChannelPipeline getPipeline() throws Exception {
            ChannelPipeline ret = super.getPipeline();
            ret.replace("handler", "handler", new play.server.ssl.SslPlayHandler() {
                @Override
                public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
                    Server.allChannels.add(e.getChannel());
                }

            });
            return ret;
        }
    }

    public class PipelineFactory extends play.server.HttpServerPipelineFactory {

        @Override
        public ChannelPipeline getPipeline() throws Exception {
            ChannelPipeline ret = super.getPipeline();
            ret.replace("handler", "handler", new play.server.PlayHandler() {
                @Override
                public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
                    Server.allChannels.add(e.getChannel());
                }

            });
            return ret;
        }
    }

    static final ChannelGroup allChannels = new DefaultChannelGroup("myServer");

    public static int httpPort = 9000;
    public final static String PID_FILE = "server.pid";
    final ExecutorService boss;
    final ExecutorService worker;
    final ChannelFactory channelFactory;
    final ServerBootstrap bootstrap;

    public void stop() throws InterruptedException {
        allChannels.disconnect().await(60, TimeUnit.SECONDS);
        allChannels.close().await(60, TimeUnit.SECONDS);
        allChannels.unbind().await(60, TimeUnit.SECONDS);
        channelFactory.releaseExternalResources();

        worker.shutdown();
        worker.awaitTermination(10, TimeUnit.SECONDS);
        boss.shutdown();
        boss.awaitTermination(10, TimeUnit.SECONDS);
        bootstrap.releaseExternalResources();
    }

    public void start() {
        System.setProperty("file.encoding", "utf-8");
        InetAddress address = null;
        try {
            address = InetAddress.getByName(System.getProperty("127.0.0.1"));
        } catch (Exception e) {
            Logger.error(e, "Could not understand http.address");
            Play.fatalServerErrorOccurred();
        }
        try {
            bootstrap.setPipelineFactory(new PipelineFactory());
            Channel c = bootstrap.bind(new InetSocketAddress(address, httpPort));
            allChannels.add(c);
            bootstrap.setOption("child.tcpNoDelay", true);
            Logger.info("Listening for HTTP on port %s ...", httpPort);
        } catch (ChannelException e) {
            Logger.error("Could not bind on port " + httpPort, e);
            Play.fatalServerErrorOccurred();
        }
        System.out.println("~ Server is up and running");
    }

}
