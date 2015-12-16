package br.com.caelum.camel;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import br.com.caelum.livraria.modelo.Livro;

/**
 * Recebe o valor de uma requisição ao JSON, e converte ele
 * na classe que contém somente os atributos que queremos
 * 
 * @author tca85
 *
 */
public class TestePooling {
	public static void main(String[] args) throws Exception {
		CamelContext ctx = new DefaultCamelContext();

		ctx.addRoutes(new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				from("http://localhost:8081/fj36-livraria/loja/livros/mais-vendidos")
				.delay(1000)
				
				// desserializando os livros que estão no JSON
				.unmarshal()
				.json()
				.process(new Processor() {
					
					@Override
					public void process(Exchange exchange) throws Exception {
						List<?> listaDeLivros = (List<?>) exchange.getIn().getBody();
						
						@SuppressWarnings("unchecked")
						ArrayList<Livro> livros = (ArrayList<Livro>) listaDeLivros.get(0);
						
						Message message = exchange.getIn();
						message.setBody(livros);
					}
				})
				
				// cria uma nova rota que recebe a lista de livros e divide em partes (cada livro)
				// como essa rota ainda não existe, vamos mockear:
				.log("${body}")
				.to("mock:livros");
			}
		});
		
		ctx.start();
		new Scanner(System.in).nextLine();
		ctx.stop();
	}
}
