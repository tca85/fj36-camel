package br.com.caelum.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

/**
 * 
 * O Apache Camel funciona como um ETL
 * 
 * @author tca85
 *
 */
public class TesteRoteamento {
	public static void main(String[] args) throws Exception {
		CamelContext context = new DefaultCamelContext();
		
		//Adicionando uma nova rota
		context.addRoutes(new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				errorHandler(deadLetterChannel("file:falha")
					       .useOriginalMessage()
					       .maximumRedeliveries(2)
					       .redeliverDelay(2000)
					       .retryAttemptedLogLevel(LoggingLevel.ERROR)
						);
				
				from("file:entrada?delay=5s")
				.log(LoggingLevel.INFO, "Processando a mensagem ${id}")
				.to("validator:file:xsd/pedido.xsd")
				.to("file:saida");
			}
		});
		
		context.start();
		Thread.sleep(30 * 1000);
		context.stop();
	}
}