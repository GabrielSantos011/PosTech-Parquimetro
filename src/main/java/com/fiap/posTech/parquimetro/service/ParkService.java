package com.fiap.posTech.parquimetro.service;

import com.fiap.posTech.parquimetro.controller.exception.ControllerNotFoundException;
import com.fiap.posTech.parquimetro.controller.exception.TrataMensagem;
import com.fiap.posTech.parquimetro.model.Endereco;
import com.fiap.posTech.parquimetro.model.Park;
import com.fiap.posTech.parquimetro.model.Pessoa;
import com.fiap.posTech.parquimetro.model.Veiculo;
import com.fiap.posTech.parquimetro.repository.EnderecoRepository;
import com.fiap.posTech.parquimetro.repository.ParkRepository;
import com.fiap.posTech.parquimetro.repository.PessoaRepository;
import com.fiap.posTech.parquimetro.repository.VeiculoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;


@Service

public class ParkService {
    private final ParkRepository parkRepository;
    private final PessoaRepository pessoaRepository;
    private final VeiculoRepository veiculoRepository;
    private final EnderecoRepository enderecoRepository;
    private final TrataMensagem mensagem;

    public ParkService(ParkRepository parkRepository, PessoaRepository pessoaRepository,
                       VeiculoRepository veiculoRepository, EnderecoRepository enderecoRepository) {
        this.parkRepository = parkRepository;
        this.pessoaRepository = pessoaRepository;
        this.veiculoRepository = veiculoRepository;
        this.enderecoRepository = enderecoRepository;
        this.mensagem = new TrataMensagem();
    }

    @Transactional
    public ResponseEntity<?> checkin(Park park) {
        try {
            Pessoa pessoa = pessoaRepository.findById(park.getPessoa().getId())
                    .orElseThrow(() -> new ControllerNotFoundException("Pessoa não encontrada"));

            Veiculo veiculo = veiculoRepository.findById(park.getVeiculo().getId())
                    .orElseThrow(() -> new ControllerNotFoundException("Veículo não encontrado"));

            Optional<Endereco> endereco;
            endereco = enderecoRepository.findById(park.getEnderecoEstacionado().getId());
            if (endereco == null) {
                cadastraEndereco(park.getEnderecoEstacionado());
            }
            LocalDateTime now = LocalDateTime.now();
            park.setEntrada(now);

            park.setAtiva(true);

            parkRepository.save(park);

            return new ResponseEntity<>(mensagem.TrataMensagemErro("Parking cadastrado com sucesso."),
                    HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace(System.out);
            return new ResponseEntity<>(mensagem.TrataMensagemErro("Erro ao cadastrar parking"),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public ResponseEntity<?> checkout(String id) {
        try {
            Park park = parkRepository.findById(id)
                    .orElseThrow(() -> new ControllerNotFoundException("Parking não encontrado"));

            LocalDateTime now = LocalDateTime.now();
            Duration drt = Duration.between(park.getEntrada(), now);

            LocalTime lt = LocalTime.ofNanoOfDay(drt.toNanos());
            var tempo = lt.format(DateTimeFormatter.ofPattern("HH:mm"));
            park.setSaida(now);
            park.setPermanencia(tempo);
            park.setAtiva(false);

            //parkRepository.save(park);

            return new ResponseEntity<>(park,
                    HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace(System.out);
            return new ResponseEntity<>(mensagem.TrataMensagemErro("Erro ao alterar parking"),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(readOnly = true)
    public Page<Park> listaTodos(Pageable page) {
        Sort sort = Sort.by("entrada").descending();
        Pageable lista = PageRequest.of(page.getPageNumber(), page.getPageSize(), sort);
        return this.parkRepository.findAll(lista);
    }

    public List<Page<Park>> getParksAtivos(Pageable pageable) {
        return parkRepository.findAllByAtivaIsTrue(pageable);
    }

    private void cadastraEndereco(Endereco enderecoEstacionado) {
        Endereco end = new Endereco();
        end.setTipoLogradouro(enderecoEstacionado.getTipoLogradouro());
        end.setLogradouro(enderecoEstacionado.getLogradouro());
        end.setNumero(enderecoEstacionado.getNumero());
        end.setComplemento(enderecoEstacionado.getComplemento());
        end.setBairro(enderecoEstacionado.getBairro());
        end.setCidade(enderecoEstacionado.getCidade());
        end.setUf(enderecoEstacionado.getUf());
        end.setCep(enderecoEstacionado.getCep());
        enderecoRepository.save(end);
    }
}
