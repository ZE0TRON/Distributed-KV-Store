package de.tum.i13.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class TestKVClient {

	@Mock
	private KVStoreClientLibrary kvStore;

	@Test
	public void testPut() {
		/*
		
		InputStreamReader is = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(is);
        
        
        OutputStreamWriter out = new OutputStreamWriter(System.out);
        BufferedWriter writer = new BufferedWriter(out);
        */
        ByteArrayInputStream in = new ByteArrayInputStream("put".getBytes());
        System.setIn(in);
        
        
        //assertEquals("EchoClient> ", System.out.);
        
        KVClient.CLI(kvStore);
        
	}
}
