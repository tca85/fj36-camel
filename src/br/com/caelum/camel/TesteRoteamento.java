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
				// DSL do Camel
				from("file:entrada?delay=5s")
				.log(LoggingLevel.INFO, "Processando a mensagem ${id}") 
				.to("file:saida");
			}
		});
		
		context.start();
		Thread.sleep(30 * 1000);
		context.stop();
	}
}