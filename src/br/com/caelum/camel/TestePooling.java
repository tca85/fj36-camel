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
import org.apache.camel.util.jndi.JndiContext;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

import br.com.caelum.livraria.modelo.Livro;

/**
 * Recebe o valor de uma requisição ao JSON, converte ele
 * na classe que contém somente os atributos que queremos
 * e depois insere em uma tabela no MySQL:
 * - create database fj36_camel
 * - create table Livros( nomeAutor TEXT );
 * 
 * O projeto fj36-livraria precisa estar rodando
 * 
 * obs: pooling roda sem parar. Teria que usar um timer para
 * evitar isso ou um event listener
 * 
 * @author tca85
 *
 */
public class TestePooling {
	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		// Cria o datasource para o MySQL que o Camel utilizará para mandar a query
		MysqlConnectionPoolDataSource mysqlDs = new MysqlConnectionPoolDataSource();
		mysqlDs.setDatabaseName("fj36_camel");
		mysqlDs.setServerName("localhost");
		mysqlDs.setPort(3306);
		mysqlDs.setUser("root");
		mysqlDs.setPassword("");
		
		JndiContext jndi = new JndiContext();
		jndi.rebind("mysqlDataSource", mysqlDs);
		
		CamelContext ctx = new DefaultCamelContext(jndi);

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
				// .log("${body}")
				//.to("mock:livros");
				
				// nova rota com o nome livros:
				.to("direct:livros");
				from("direct:livros")
				.split(body())
				.process(new Processor() {
					
					@Override
					public void process(Exchange exchange) throws Exception {
						Message inbound = exchange.getIn();
						
						Livro livro = (Livro) inbound.getBody();
						
						String nomeAutor = livro.getNomeAutor();
						inbound.setHeader("nomeAutor", nomeAutor);	
					}
				})
				
				// insere um por um na tabela Livros, criada no database fj36_camel
				// insert into Livros (nomeAutor) values (:nomeAutor)
				//.setBody(simple("insert into Livros (nomeAutor) values ('${header[nomeAutor]}')"))
				.setBody(simple("insert into Livros (nomeAutor) values (:nomeAutor)"))
				.to("jdbc:mysqlDataSource");
			}
		});
		
		ctx.start();
		new Scanner(System.in).nextLine();
		ctx.stop();
	}
}