package com.teamProject2.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.teamProject2.entity.ClientDto;
import com.teamProject2.repository.ClientRepository;
import com.teamProject2.repository.InventoryRepository;

@Service
public class ClientService {
	
	public final ClientRepository clientRepository;	
	public ClientService(ClientRepository clientRepository) {
		this.clientRepository = clientRepository;
	}
	
	/**
	 * 공급처 목록
	 */
	public List<ClientDto> supList(){
		return clientRepository.findAll();
	}

}
