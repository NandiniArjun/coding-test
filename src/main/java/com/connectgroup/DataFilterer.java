package com.connectgroup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DataFilterer {
	private static final Pattern logExtractRegEx = Pattern.compile("(([\\d]+),([A-Z]+),([\\d]+))|"
			+ "(([\\d]+),([\\d]+),([A-Z]+))|(([A-Z]+),([\\d]+),([\\d]+))");
	
	private static int index = -1;

    public static Collection<?> filterByCountry(Reader source, String country) {
    	List<String> lines = getLinesFromReader(source);
    	return lines.stream()
                .filter(logExtractRegEx.asPredicate().and(line -> line.contains(country)))
                .collect(Collectors.toList());
    }

    public static Collection<?> filterByCountryWithResponseTimeAboveLimit(Reader source, String country, long limit) {
    	List<String> lines = getLinesFromReader(source);
		return lines.stream() 
		.filter(logExtractRegEx.asPredicate().and(line -> line.contains(country))) 
		.map(line -> line.split(","))
		.filter(line -> Long.valueOf(line[index]) > limit)
		.collect(Collectors.toList());
    }
    
    public static Collection<?> filterByResponseTimeAboveAverage(Reader source) {
    	AtomicLong sum = new AtomicLong(0);
    	
    	List<String> lines = getLinesFromReader(source);
    	
		List<Long> responseTime = lines.stream()
		.filter(logExtractRegEx.asPredicate()) 
		.map(line -> { 
			String[] str = line.split(","); 
			return Long.valueOf(str[index]);
		}).collect(Collectors.toList());

		for(Long response : responseTime) {
    		sum.addAndGet(response);
    	}
    	
    	return lines.stream()
    			.filter(logExtractRegEx.asPredicate())
    			.map(line -> line.split(","))
    			.filter(line -> Long.valueOf(line[index]) > (sum.longValue()/responseTime.size()))
    			.collect(Collectors.toList());
   }
    
    private static List<String> getLinesFromReader(Reader source) {

        final BufferedReader br = new BufferedReader(source);
        final List<String> lines = new ArrayList<>();

        String line;

        try {
            while ((line = br.readLine()) != null) {
            	if(index == -1) {
            		String[] headers = line.split(",");
            		for(String header : headers) {
            			index++;
            			if(header.equals("RESPONSE_TIME")) break;
            		}
            	} else {
            		lines.add(line);
            	}
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }
}
