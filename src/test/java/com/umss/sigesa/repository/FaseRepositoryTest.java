package com.umss.sigesa.repository;

import com.umss.sigesa.config.JpaAuditingConfig;
import com.umss.sigesa.domain.model.Fase;
import com.umss.sigesa.domain.model.ModalidadAcreditacion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
class FaseRepositoryTest {

    @Autowired
    private FaseRepository faseRepository;

    @Test
    void debePersistirFaseRaizYSubfase() {
        Fase raiz = faseRepository.save(Fase.builder()
                .codigo("TEST-ROOT")
                .nombre("Raíz test")
                .modalidad(ModalidadAcreditacion.ARCUSUR)
                .orden(1)
                .build());

        Fase subfase = faseRepository.save(Fase.builder()
                .codigo("TEST-SUB")
                .nombre("Sub test")
                .modalidad(ModalidadAcreditacion.ARCUSUR)
                .orden(1)
                .parent(raiz)
                .build());

        assertThat(faseRepository.findByIdAndDeletedAtIsNull(raiz.getId())).isPresent();
        assertThat(faseRepository.findByIdAndDeletedAtIsNull(subfase.getId())).isPresent();
        assertThat(faseRepository.countByParentIdAndDeletedAtIsNull(raiz.getId())).isEqualTo(1L);
    }

    @Test
    void specification_debeFiltrarPorModalidadYTexto() {
        faseRepository.save(Fase.builder()
                .codigo("SPEC-ARC")
                .nombre("Autoevaluación UMSS")
                .modalidad(ModalidadAcreditacion.ARCUSUR)
                .build());
        faseRepository.save(Fase.builder()
                .codigo("SPEC-CEUB")
                .nombre("Evaluación CEUB")
                .modalidad(ModalidadAcreditacion.CEUB)
                .build());

        Specification<Fase> spec = FaseSpecifications.combinar(
                FaseSpecifications.esFaseRaiz(),
                FaseSpecifications.conModalidad(ModalidadAcreditacion.ARCUSUR),
                FaseSpecifications.incluirEliminadas(false),
                FaseSpecifications.buscarPorTexto("umss")
        );

        Page<Fase> result = faseRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getCodigo()).isEqualTo("SPEC-ARC");
    }

    @Test
    void softDelete_debeExcluirDeConsultasActivas() {
        Fase fase = faseRepository.save(Fase.builder()
                .codigo("SOFT-DEL")
                .nombre("Eliminable")
                .modalidad(ModalidadAcreditacion.CEUB)
                .deletedAt(LocalDateTime.now())
                .build());

        assertThat(faseRepository.findByIdAndDeletedAtIsNull(fase.getId())).isEmpty();
        assertThat(faseRepository.existsByCodigoAndDeletedAtIsNull("SOFT-DEL")).isFalse();
    }
}
