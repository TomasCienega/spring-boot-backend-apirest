package com.bolsadeideas.springboot.backend.apirest.controllers;

import ch.qos.logback.core.net.server.Client;
import com.bolsadeideas.springboot.backend.apirest.models.entity.Cliente;
import com.bolsadeideas.springboot.backend.apirest.models.services.IClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"http://localhost:4200"})
@RestController
@RequestMapping("/api")
public class ClienteRestController {

    @Autowired
    private IClienteService clienteService;
    @GetMapping("/clientes")
    public List<Cliente> index(){
        return clienteService.findAll();
    }
    @GetMapping("/clientes/page/{page}")
    public Page<Cliente> index(@PathVariable Integer page){

        Pageable pageable =PageRequest.of(page,4);
        return clienteService.findAll(pageable);
    }
    @GetMapping("/clientes/{id}")
    public ResponseEntity<?> show(@PathVariable Long id){
        Cliente cliente = null;
        Map<String, Object> response = new HashMap<>();
        try {
            cliente=clienteService.findById(id);
        } catch (DataAccessException e){
            response.put("mensaje", "Error al realizar la consulta en la base de datos");
            response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (cliente == null) {
            response.put("mensaje", "El cliente ID: ".concat(id.toString().concat(" no existe en la base de datos!")));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Cliente>(cliente,HttpStatus.OK);
    }
    @PostMapping("/clientes")
    public ResponseEntity<?> create(@Valid @RequestBody  Cliente cliente, BindingResult result){
        Cliente newClient  =null;
        Map<String, Object> response = new HashMap<>();
        if (result.hasErrors()){
            /*List<String> errors = new ArrayList<>();
            for (FieldError err: result.getFieldErrors()){
                errors.add("El campo '" + err.getField() + "' " + err.getDefaultMessage());
            }*/
            List<String> errors =result.getFieldErrors()
                    .stream()
                    .map(err-> "El campo '" + err.getField() + "' " + err.getDefaultMessage())
                    .collect(Collectors.toList());
            response.put("errors", errors);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST);
        }
        try {
            newClient=clienteService.save(cliente);
        }catch (DataAccessException e){
            response.put("mensaje", "Error al realizar el insert en la base de datos");
            response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        response.put("mensaje", ": Ah sido creado con exito!");
        response.put("cliente",newClient);
        return new ResponseEntity<Map<String, Object>>(response,HttpStatus.CREATED);
    }

    @PutMapping("/clientes/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody Cliente cliente, BindingResult result, @PathVariable Long id){

        Cliente clienteActual = clienteService.findById(id);
        Cliente clienteUpdated =null;
        Map<String, Object> response = new HashMap<>();
        if (result.hasErrors()){
            List<String> errors =result.getFieldErrors()
                    .stream()
                    .map(err-> "El campo '" + err.getField() + "' " + err.getDefaultMessage())
                    .collect(Collectors.toList());
            response.put("errors", errors);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST);
        }

        if (clienteActual == null) {
            response.put("mensaje", "Error: no se pudo editar, el cliente ID: ".concat(id.toString().concat(" no existe en la base de datos!")));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
        }
        try {
            clienteActual.setNombre(cliente.getNombre());
            clienteActual.setApellido(cliente.getApellido());
            clienteActual.setEmail(cliente.getEmail());
            clienteActual.setCreateAt(cliente.getCreateAt());

            clienteUpdated=clienteService.save(clienteActual);

        }catch (DataAccessException e){
            response.put("mensaje", "Error al actualizar el cliente en la base de datos");
            response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        response.put("mensaje", ": Ah sido actualizado con exito!");
        response.put("cliente",clienteUpdated);
        return new ResponseEntity<Map<String, Object>>(response,HttpStatus.CREATED);
    }
    @DeleteMapping("/clientes/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        Map<String,Object> response = new HashMap<>();
        try {
            if (clienteService.findById(id)==null){
                response.put("mensaje", "El Id que seleccionaste no existe");
                return new ResponseEntity<Map<String,Object>>(response, HttpStatus.NOT_FOUND);
            }else {
                clienteService.delete(id);
                response.put("mensaje", "El cliente ha sido eliminado con éxito!");
                return new ResponseEntity<Map<String,Object>>(response,HttpStatus.OK);
            }
        }catch (DataAccessException e){
            response.put("mensaje", "Error al eliminar el cliente de la base de datos");
            response.put("error",e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String,Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
