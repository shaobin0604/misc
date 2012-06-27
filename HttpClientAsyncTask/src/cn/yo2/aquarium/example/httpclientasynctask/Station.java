package cn.yo2.aquarium.example.httpclientasynctask;

public class Station {
    long id;
    String name;
    String description;
    String streamUrl;
    String streamCodec;
    String streamCodeRate;
    String sinaWeibo;
    boolean isStable;
    String logoUrl;
    String frequency;
    String vtag;
    String keywords;
    long regionId;
    String regionName;
    long typeId;
    String typeName;
    long favoriteCount;
    
    
    public Station(long id, String name, String description, String streamUrl, String streamCodec,
            String streamCodeRate, String sinaWeibo, boolean isStable, String logoUrl, String frequency,
            String vtag, String keywords, long regionId, String regionName, long typeId, String typeName,
            long favoriteCount) {
        super();
        this.id = id;
        this.name = name;
        this.description = description;
        this.streamUrl = streamUrl;
        this.streamCodec = streamCodec;
        this.streamCodeRate = streamCodeRate;
        this.sinaWeibo = sinaWeibo;
        this.isStable = isStable;
        this.logoUrl = logoUrl;
        this.frequency = frequency;
        this.vtag = vtag;
        this.keywords = keywords;
        this.regionId = regionId;
        this.regionName = regionName;
        this.typeId = typeId;
        this.typeName = typeName;
        this.favoriteCount = favoriteCount;
    }
    
    
}
