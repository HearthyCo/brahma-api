package utils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import gl.glue.brahma.util.RedisHelper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.net.MalformedURLException;
import java.net.URL;


public class FakeRedisHelper extends RedisHelper {

    private static Config conf = null;
    private static JedisPool pool;

    static {
        conf = ConfigFactory.load();
    }

    @Override
    public Jedis getResource() {

        // Defaults
        String host = "localhost";
        int port = 6379;

        URL url = null;
        try {
            String uri = conf.getString("redis.uri").replace("tcp", "http");
            url = new URL(uri);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if (url != null) {
            host = url.getHost();
            port = url.getPort();
        }

        pool = new JedisPool(new JedisPoolConfig(), host, port);

        return pool.getResource();
    }

    public String clearAll() {
        if (pool == null) initRedis();
        return pool.getResource().flushAll();
    }

}
