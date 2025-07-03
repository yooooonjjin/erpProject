package com.teamProject2.service;

import org.springframework.stereotype.Service;

import com.teamProject2.repository.ClientRepository;

@Service
public class ClientService {

	public final ClientRepository clientRepository;
	
	public ClientService(ClientRepository clientRepository) {
		this.clientRepository = clientRepository;
	}
}
