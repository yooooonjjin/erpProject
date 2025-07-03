package com.myerp.service;

import org.springframework.stereotype.Service;

import com.myerp.repository.ClientRepository;

@Service
public class ClientService {

	public final ClientRepository clientRepository;
	
	public ClientService(ClientRepository clientRepository) {
		this.clientRepository = clientRepository;
	}
}
