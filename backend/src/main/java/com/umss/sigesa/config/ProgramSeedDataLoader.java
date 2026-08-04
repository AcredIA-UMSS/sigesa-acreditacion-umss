package com.umss.sigesa.config;

import com.umss.sigesa.adapter.out.persistance.entity.ProgramJpaEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataProgramRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Catálogo de carreras UMSS para FSD-UC-003 (procesos CEUB / ARCU-SUR sobre carreras reales).
 */
@Component
@Profile("!prod")
@Order(80)
public class ProgramSeedDataLoader implements ApplicationRunner {

    private final SpringDataProgramRepository programRepository;

    public ProgramSeedDataLoader(SpringDataProgramRepository programRepository) {
        this.programRepository = programRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        seed(DevSeedData.PROGRAM_INF_SIS, "INF-SIS", "Ingeniería de Sistemas", "FCT");
        seed(DevSeedData.PROGRAM_ING_CIVIL, "ING-CIV", "Ingeniería Civil", "FCT");
        seed(DevSeedData.PROGRAM_ING_QUIMICA, "ING-QUI", "Ingeniería Química", "FCT");
        seed(DevSeedData.PROGRAM_ING_INDUSTRIAL, "ING-IND", "Ingeniería Industrial", "FCT");
        seed(DevSeedData.PROGRAM_ING_ELECTRONICA, "ING-ELE", "Ingeniería Electrónica", "FCT");
        seed(DevSeedData.PROGRAM_ING_MECANICA, "ING-MEC", "Ingeniería Mecánica", "FCT");
        seed(DevSeedData.PROGRAM_ING_AMBIENTAL, "ING-AMB", "Ingeniería Ambiental", "FCT");
        seed(DevSeedData.PROGRAM_ING_PETROLEO, "ING-PET", "Ingeniería de Petróleo y Gas Natural", "FCT");
        seed(DevSeedData.PROGRAM_ING_MINAS, "ING-MIN", "Ingeniería en Minas", "FCT");
        seed(DevSeedData.PROGRAM_ING_TELECOM, "ING-TEL", "Ingeniería de Telecomunicaciones", "FCT");
        seed(DevSeedData.PROGRAM_ARQUITECTURA, "ARQ", "Arquitectura", "FAU");
        seed(DevSeedData.PROGRAM_MEDICINA, "MED", "Medicina", "FCMM");
        seed(DevSeedData.PROGRAM_ENFERMERIA, "ENF", "Enfermería", "FCMM");
        seed(DevSeedData.PROGRAM_MED_VET, "MED-VET", "Medicina Veterinaria", "FCMM");
        seed(DevSeedData.PROGRAM_DERECHO, "DER", "Derecho", "CCJJ");
        seed(DevSeedData.PROGRAM_CONTADURIA, "C-PUB", "Contaduría Pública", "CCJJ");
        seed(DevSeedData.PROGRAM_ADM_EMPRESAS, "ADM-EMP", "Administración de Empresas", "CCJJ");
        seed(DevSeedData.PROGRAM_ECONOMIA, "ECO", "Economía", "CCJJ");
        seed(DevSeedData.PROGRAM_PSICOLOGIA, "PSI", "Psicología", "FACSO");
        seed(DevSeedData.PROGRAM_SOCIOLOGIA, "SOC", "Sociología", "FACSO");
        seed(DevSeedData.PROGRAM_COM_SOCIAL, "COM-SOC", "Comunicación Social", "FACSO");
        seed(DevSeedData.PROGRAM_TRABAJO_SOCIAL, "TRA-OFT", "Trabajo Social", "FACSO");
        seed(DevSeedData.PROGRAM_BIOLOGIA, "BIO", "Biología", "FCyt");
        seed(DevSeedData.PROGRAM_ING_AGRONOMICA, "ING-AGR", "Ingeniería Agronómica", "FAFCYT");
        seed(DevSeedData.PROGRAM_ING_ALIMENTOS, "ING-ALI", "Ingeniería en Alimentos", "FCT");
    }

    private void seed(UUID id, String code, String name, String faculty) {
        if (programRepository.existsById(id)) {
            return;
        }
        programRepository.save(ProgramJpaEntity.builder()
                .id(id)
                .code(code)
                .name(name)
                .faculty(faculty)
                .active(true)
                .build());
    }
}
