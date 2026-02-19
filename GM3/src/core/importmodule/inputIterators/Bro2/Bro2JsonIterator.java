package core.importmodule.inputIterators.Bro2;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import core.fingerprint.PMetaData;
import core.fingerprint.PacketData;
import core.importmodule.ImportItem;
import core.logging.Logger;
import core.logging.Severity;
import util.Cidr;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class Bro2JsonIterator implements Iterator<PacketData> {

    private static final Gson gson = new Gson();
    private static final Type MAP_TYPE = new TypeToken<HashMap<String, Object>>(){}.getType();

    private final ImportItem source;

    private final Path inPath;

    private boolean done;

    private BlockingQueue<PacketData> packetQueue;

    private long size;

    long idxLine;

    private Bro2JsonIterator(ImportItem source, Path inPath) {
        this.source = source;
        this.inPath = inPath;
        done = false;
        this.packetQueue = new ArrayBlockingQueue<>(100);

        idxLine = 0;
    }

    public long getSize() {
        return this.size;
    }

    @Override
    public boolean hasNext() {
        return !(done && packetQueue.isEmpty());
    }

    @Override
    public PacketData next() {
        return packetQueue.poll();
    }

    public static Iterator<PacketData> getBro2JsonIterator(ImportItem source, Path inPath) throws IllegalStateException{
        Bro2JsonIterator iterator = new Bro2JsonIterator(source, inPath);

        final AtomicLong size = new AtomicLong(0);
        try (final BufferedReader reader = Files.newBufferedReader(inPath)) {
            reader.lines().filter(line -> !line.startsWith("#")).forEach(line -> size.addAndGet(2));
        } catch (IOException ioe) {
            throw new IllegalStateException("Error reading File: " + inPath);
        }

        iterator.size = size.get();

        if (size.get() > 0) {
            iterator.parseFile();
        }

        return iterator;
    }

    private void parseFile() {
        new Thread(() -> {
            try (final BufferedReader reader = Files.newBufferedReader(inPath)) {
                reader.lines().forEach(this::parseLine);
                done = true;
            } catch (IOException ioe) {
                done = true;
                Logger.log(this, Severity.Error, "Error reading File: " + inPath);
            }
        }).start();
    }

    private void parseLine(String lineText){
        idxLine++;
        lineText = lineText.trim();
        if(lineText.isEmpty()) {
            return;
        }

        try {
            HashMap<String, Object> lineParsed = gson.fromJson(lineText, MAP_TYPE);

            String tsValue = String.valueOf(lineParsed.get("ts"));
            String[] timeComps = tsValue.split("\\.");
            String timeString = timeComps[0] + (timeComps.length > 1 ? (timeComps[1] + "000").substring(0, 3) : "");
            long time = Long.parseLong(timeString);
            int dstPort = Integer.parseInt(String.valueOf(lineParsed.get("id.resp_p")).replaceAll("\\.0$", ""));
            int srcPort = Integer.parseInt(String.valueOf(lineParsed.get("id.orig_p")).replaceAll("\\.0$", ""));
            short proto = parseProtocol(String.valueOf(lineParsed.get("proto")));

            int sizePacket = -1;
            if (lineParsed.containsKey("orig_bytes") && lineParsed.get("orig_bytes") != null) {
                sizePacket = Integer.parseInt(String.valueOf(lineParsed.get("orig_bytes")).replaceAll("\\.0$", ""));
            }

            Cidr srcIp = new Cidr(parseIp(String.valueOf(lineParsed.get("id.orig_h"))));
            Cidr dstIp = new Cidr(parseIp(String.valueOf(lineParsed.get("id.resp_h"))));

            PMetaData meta = new PMetaData(source, time, idxLine, srcPort, dstPort, proto, srcIp, null, dstIp, null, -1, sizePacket, 2048, -1, -1, -1, -1, null);

            PacketData data = new PacketData(1, meta);

            try {
                this.packetQueue.put(data);
            } catch (InterruptedException ie) {
                // don't care
            }
        } catch(JsonSyntaxException | NumberFormatException ex) {
            Logger.log(this, Severity.Warning, "There was an error processing a line in [" + this.inPath + "]: " + ex.getMessage());
        }
    }

    private static short parseProtocol(String protocol) {
        if( "udp".equalsIgnoreCase(protocol) ) {
            return 17;
        }
        if( "tcp".equalsIgnoreCase(protocol) ) {
            return 6;
        }
        return -1;
    }

    private static Byte[] parseIp(String ip) {
        // String.split() takes a regex, not a string.
        return Arrays.stream(ip.split("\\.")).map(octet -> Integer.valueOf(octet).byteValue()).collect(Collectors.toList()).toArray(new Byte[4]);
    }
}
