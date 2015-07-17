package kafkaWork;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;
import java.util.zip.ZipEntry;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.util.zip.ZipInputStream;


class kafkaSampleProducer{
	public static void main(String[] args) {
     
 
       Properties props = new Properties();
        props.put("metadata.broker.list", "cheng-vpc-1:9092,cheng-vpc-2:9092:cheng-vpc-3:9092");
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("request.required.acks", "1");
        props.put("producer.type","async");
        props.put("client.id", "pi_ifutil_raw");
        props.put("queue.buffering.max.messages","130181");
 
       ProducerConfig config = new ProducerConfig(props);

        Producer<String, String> producer = new Producer<String, String>(config);
        
        InputStream file = null;
        ZipInputStream zipStream = null;
        int cnt = 0;
        
        double ifutil_data_size = 614104;
        double ifutil_raw_size = 14048047;
        double qos_stats_size = 14668113;
        double qos_raw_size = 14475499;
        
        double ifutil_data_count = 3739;
        double ifutil_raw_count = 130181;
        double qos_stats_count =  134607;
        double qos_raw_count  = 133026;
        
			try {
				while(true){
				cnt += 1;
				System.out.println("Times: " + cnt);
				file = new FileInputStream(args[0]);
				 zipStream = new ZipInputStream(file);
			     ZipEntry ze;
			     ByteArrayOutputStream bos = new ByteArrayOutputStream();
			     byte[] buf = new byte[1024];
			     String regexp = "\\.csv|\\_[0-9]+\\_[0-9]+";
			     double start_time, elapsedTime;
			     double file_size;
			     KeyedMessage<String, String> data;
			     double count;
			     Random rnd = new Random();
			     while((ze = zipStream.getNextEntry())!=null){
			    	 	count = 0;
			        	String filename = ze.getName();
			        	for(int len; (len=zipStream.read(buf)) > 0;){
			        		bos.write(buf,0,len);
			        	}
			        	
			        	file_size = bos.size();
			        	if (args[1].equals("LINE")){
			        		if (filename.contains("pi_ifutil_raw")){
				        		System.out.println(filename + " is processing...["+ args[1]+ "] " + (int)file_size);
					        	String[] lines = bos.toString().split("\n");
					        	start_time = System.nanoTime();
					        	for( String line : lines){
					        		if(count == 0){
					        			count++;
					        			continue;
					        		}else{
					        			count++;
					        		}
					        		if(count == 9000){
					        			break;
					        		}
					        		data = new KeyedMessage<String, String>(filename.replaceAll(regexp, "")+"_bkp", String.valueOf(rnd.nextInt()), line);
						        	producer.send(data);
					        	}
					        	elapsedTime = System.nanoTime() - start_time;
					        	System.out.println(filename + " is completed!" +" "+ file_size/1024/1024/(elapsedTime/1000000000) + " MB/s " + ifutil_raw_count/(elapsedTime/1000000000)+" records/s");
				        	}
			        		bos.reset();
			        	}else{
			        		System.out.println(filename + " is processing...["+ args[1]+ "] " + (int)file_size);
			        		start_time = System.nanoTime();
			        		data = new KeyedMessage<String, String>(filename.replaceAll(regexp, ""), "0.0.0.0", bos.toString());
			        		bos.reset();
			        		producer.send(data);
			        		elapsedTime = System.nanoTime() - start_time;
			        		System.out.println(filename + " is completed!" + " total count:"+ count +  " " + file_size/1024/1024/(elapsedTime/1000000000) + " MB/s " + count/(elapsedTime/1000000000)+" records/s");
				        	
			        	}

			        	
			     }
			     //Thread.sleep(20000);
				}
			} catch (Exception e) {
				System.out.println(e.toString());
			}finally{
					try {
						zipStream.close();
						producer.close();
						file.close();
					} catch (IOException e) {
						System.out.println(e.toString());
					}
			}
			
			
        
    }
}


