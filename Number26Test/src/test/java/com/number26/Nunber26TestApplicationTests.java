package com.number26;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.number26.models.Transaction;
import com.number26.repos.TransactionRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Number26TestApplication.class)
@WebAppConfiguration
public class Nunber26TestApplicationTests {
	
	@Autowired
	private TransactionRepository transactionRepo;
	
    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    private MockMvc mockMvc;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream().filter(
                hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().get();

        Assert.assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }

    
    @Before
    public void setup() throws Exception {
    	mockMvc = webAppContextSetup(webApplicationContext).build();
    }
    
	@Test
	public void testCreateTransaction() throws Exception {
		Transaction transactionToCreate = new Transaction(null, "Test", new Double(2000), null);
		
		mockMvc.perform(put("/transactionservice/1")
				.content(this.json(transactionToCreate))
				.contentType(contentType))
		.andExpect(status().isOk())
		.andExpect(content().contentType(contentType))
		.andExpect(content().bytes("{\"status\":\"ok\"}".getBytes()));
		
		Transaction failingTransaction = new Transaction(null, "Test", new Double(2000), new Long(2));
		mockMvc.perform(put("/transactionservice/2")
				.content(this.json(failingTransaction))
				.contentType(contentType))
		.andExpect(status().isOk())
		.andExpect(content().contentType(contentType))
		.andExpect(content().bytes("{\"status\":\"ko\"}".getBytes()));
	}
	
	@Test
	public void testGetTransaction() throws Exception {
		transactionRepo.checkAndSave(new Transaction(new Long(0), "Test", new Double(1234), null));
		
		mockMvc.perform(get("/transactionservice/0"))
		.andExpect(status().isOk())
		.andExpect(content().contentType(contentType))
		.andExpect(content().bytes("{\"id\":0,\"type\":\"Test\",\"amount\":1234.0,\"parentId\":null}".getBytes()));
	}
	
	@Test
	public void testGetTransactionsIdsByType() throws Exception {
		List<Long> ids = new ArrayList<>();
		for(int i = 0; i < 5; i++){
			Long id = new Long(i);
			transactionRepo.checkAndSave(new Transaction(id, "Test", new Double(1234), null));
			ids.add(id);
		}
		String idsList = ids.toString();
		mockMvc.perform(get("/transactionservice/types/Test"))
		.andExpect(status().isOk())
		.andExpect(content().contentType(contentType))
		.andExpect(content().bytes("[0,1,2,3,4]".getBytes()));
	}
	
	@Test
	public void testGetSumTransactions() throws Exception {
		Random r = new Random();
		Double sum = new Double(0);
		Long id = new Long(0);
		Long previousTransactionId = null;
		for(int i = 0; i < 5; i++){
			Double amount = new Double(r.nextInt());
			Transaction transaction = transactionRepo.checkAndSave(new Transaction(id++, "Test", amount, previousTransactionId));
			previousTransactionId = transaction.getId();
			sum += amount;
		}
		
		mockMvc.perform(get("/transactionservice/sum/0"))
		.andExpect(status().isOk())
		.andExpect(content().contentType(contentType))
		.andExpect(content().bytes(String.format("{\"sum\":\"%s\"}", sum.toString()).getBytes()));
		
		// status ko if the transaction does not exist
		mockMvc.perform(get("/transactionservice/sum/1234"))
		.andExpect(status().isOk())
		.andExpect(content().contentType(contentType))
		.andExpect(content().bytes("{\"status\":\"ko\"}".getBytes()));
	}
	
	
    protected String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
	


}
