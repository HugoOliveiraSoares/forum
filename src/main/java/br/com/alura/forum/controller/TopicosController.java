package br.com.alura.forum.controller;

import br.com.alura.forum.controller.dto.DetalhesTopicoDTO;
import br.com.alura.forum.controller.dto.TopicoDTO;
import br.com.alura.forum.controller.form.AtualizaTopicoForm;
import br.com.alura.forum.controller.form.TopicoForm;
import br.com.alura.forum.modelo.Topico;
import br.com.alura.forum.repository.CursoRepository;
import br.com.alura.forum.repository.TopicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/topicos")
public class TopicosController {

    @Autowired
    private TopicoRepository topicoRepository;

    @Autowired
    private CursoRepository cursoRepository;

//    @RequestMapping(method = RequestMethod.GET, value = "/topicos")
//    @RequestMapping(method = RequestMethod.GET, value = "/topicos")
    @GetMapping
    @Cacheable(value = "listaTopicos")
    public Page<TopicoDTO> lista(@RequestParam(required = false) String nomeCurso,
                                 @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable paginacao){

        if (nomeCurso == null) {
            Page<Topico> topicos = topicoRepository.findAll(paginacao);
            return TopicoDTO.converter(topicos);
        }
        else {
            Page<Topico> topicos = topicoRepository.findByCurso_Nome(nomeCurso, paginacao);
            return TopicoDTO.converter( topicos );
        }

    }

    @PostMapping
    @Transactional // Nessecario para atualizar no campo
    @CacheEvict(value = "listaTopicos", allEntries = true)
    public ResponseEntity<TopicoDTO> cadastrar(@RequestBody @Valid TopicoForm topicoForm, UriComponentsBuilder uriComponentsBuilder){

        Topico topico = topicoForm.converter(cursoRepository);
        topicoRepository.save(topico);

        URI uri = uriComponentsBuilder.path("topicos/{id}").buildAndExpand(topico.getId()).toUri();
        return ResponseEntity.created(uri).body(new TopicoDTO(topico));

    }

    @GetMapping("/{id}")
    public ResponseEntity<DetalhesTopicoDTO> detalhar(@PathVariable Long id){

        Optional<Topico> topico = topicoRepository.findById(id);

        if (topico.isPresent())
            return ResponseEntity.ok(new DetalhesTopicoDTO(topico.get()));
        else
            return ResponseEntity.notFound().build();

    }

    @PutMapping("/{id}")
    @Transactional // Nessecario para atualizar no campo
    @CacheEvict(value = "listaTopicos", allEntries = true)
    public ResponseEntity<TopicoDTO> atualizar(@PathVariable Long id, @RequestBody @Valid AtualizaTopicoForm form){

        Optional<Topico> opTopico = topicoRepository.findById(id);

        if (opTopico.isPresent()) {
            Topico topico = form.atualizar(id, topicoRepository);
            return ResponseEntity.ok(new TopicoDTO(topico));
        } else
            return ResponseEntity.notFound().build();


    }

    @DeleteMapping("/{id}")
    @Transactional // Nessecario para atualizar no campo
    @CacheEvict(value = "listaTopicos", allEntries = true)
    public ResponseEntity<?> remover(@PathVariable Long id){

        Optional<Topico> opTopico = topicoRepository.findById(id);

        if (opTopico.isPresent()) {
            topicoRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else
            return ResponseEntity.notFound().build();

    }

}


