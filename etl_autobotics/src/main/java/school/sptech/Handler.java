package school.sptech;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.RequestHandler;

    public class Handler implements RequestHandler<S3Event, String> {
        @Override
        public String handleRequest(S3Event event, Context context) {
            event.getRecords().forEach(record -> {
                String bucket = record.getS3().getBucket().getName();
                String key = record.getS3().getObject().getKey();
                context.getLogger().log("Novo objeto " + bucket + "/" + key);
            });

            try{
                Main.rodarTratamento();
                return "o tratamento acabou.";
            } catch (Exception e) {
                context.getLogger().log("erro na hora de tratar: " + e.getMessage());
                return "tratamento deu errado";
            }
        }
    }

