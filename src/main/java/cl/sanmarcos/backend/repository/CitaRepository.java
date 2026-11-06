package cl.sanmarcos.backend.repository;

import cl.sanmarcos.backend.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository ya trae findAll, findById, save y deleteById.
public interface CitaRepository extends JpaRepository<Cita, Long> {
}
