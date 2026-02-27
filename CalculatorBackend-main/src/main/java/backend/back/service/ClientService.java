package backend.back.service;

import backend.back.dto.request.ClientRequest;
import backend.back.dto.response.ClientResponse;
import backend.back.entity.Client;
import backend.back.entity.User;
import backend.back.exception.ResourceNotFoundException;
import backend.back.mapper.ClientMapper;
import backend.back.repository.ClientRepository;
import backend.back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ClientMapper clientMapper;

    public List<ClientResponse> getAllClients() {
        return clientRepository.findAll().stream()
                .map(clientMapper::toResponse)
                .toList();
    }

    public ClientResponse getById(Long id) {
        return clientMapper.toResponse(findById(id));
    }

    @Transactional
    public ClientResponse create(ClientRequest request) {
        Client client = new Client();
        fillClient(client, request);
        client.setCreatedBy(currentUser());
        client.setCreatedAt(LocalDateTime.now());
        return clientMapper.toResponse(clientRepository.save(client));
    }

    @Transactional
    public ClientResponse update(Long id, ClientRequest request) {
        Client client = findById(id);
        fillClient(client, request);
        return clientMapper.toResponse(clientRepository.save(client));
    }

    @Transactional
    public void delete(Long id) {
        clientRepository.delete(findById(id));
    }

    private void fillClient(Client client, ClientRequest req) {
        client.setLastName(req.getLastName());
        client.setFirstName(req.getFirstName());
        client.setPatronymic(req.getPatronymic());
        client.setPhone(req.getPhone());
        client.setEmail(req.getEmail());
        client.setAddress(req.getAddress());
    }

    private Client findById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Клиент", id));
    }

    private User currentUser() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByLogin(login).orElseThrow();
    }
}