package com.app.Service;

import Infrastructure.security.SessionManager;
import com.app.Model.Dao.ClienteDao;
import com.app.Model.domain.Cliente;
import com.app.Service.exceptions.ServiceException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClienteService {
    private final ClienteDao clienteDao = new ClienteDao();

    public List<Cliente> getAll() throws Exception {
        try{
            return  clienteDao.findAll();
        }catch (Exception e){
            throw new Exception("error carga clientes:" + e.getMessage());

        }
    }
    public Cliente findById(int id) throws ServiceException {
        try{
            return (Cliente) clienteDao.findById(id)
                    .orElseThrow(() -> new ServiceException("Cliente no encottado con id:" + id));
        } catch (Exception e){
            throw new ServiceException("error buscar clientes:" + e.getMessage());
        }
    }
    public Cliente create(Cliente cliente) throws ServiceException{
        validate(cliente);
        try{
            return clienteDao.save(cliente);
        }catch (SQLException e){
            throw new ServiceException("Error al crear nuevo cliente"+ e.getMessage());
        }
    }
    public void update(Cliente cliente) throws Exception {
        requireAdmin("Editar cliente");
        validate(cliente);
        try{
            boolean update = clienteDao.update(cliente);
            if (!update) throw new ServiceException("Cliente no encontrado con id:" + cliente.getId());

        }catch (SQLException e){
            throw new ServiceException("Error al actulziar datos del cliente" +e.getMessage());
        }
    }

    public void delete(int id) throws Exception {
        requireAdmin("Eliminar cliente");
        try{
            boolean deleted = clienteDao.delete(id);
            if(!deleted) throw new ServiceException("Cliente no se encotro por id" + id);
        }catch (SQLException e){
            throw new ServiceException("Error al eliminar cliente: " + e.getMessage());
        }
    }

    private void requireAdmin(String action) throws ServiceException {
        if(!SessionManager.getInstance().isAdmin())
            throw new ServiceException("Solo el administrador puede" + action );
    }

    private void validate(Cliente cliente) throws ServiceException {
        List<String> errors = new ArrayList<>();
        check(cliente.getFirstName(),"El nombre es obligatorio", errors);
        check(cliente.getLastName(),"El apellido es obligatorio",errors);
        check(cliente.getEmail(), "El correrio es obligatorio",errors);
        check(cliente.getPhone(),"el numero de telefeno es olbligatorio", errors);
        if (!errors.isEmpty()) {
            throw new ServiceException(String.join(", ",errors));
        }
    }
    private void check(String value, String errorMsg, List<String> errors) {
        if (value == null || value.isEmpty()) {
            errors.add(errorMsg);
        }
    }

}
