package xyz.spedcord.server.job;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

public class Location {

    public double x, y, z;

    @BsonCreator
    public Location(@BsonProperty("x") double x, @BsonProperty("y") double y, @BsonProperty("z") double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

}
