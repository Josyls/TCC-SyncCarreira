package com.synccarreira.synccarreira_api.entities.enums;

/**
 * Situação de uma trilha dentro de uma jornada (RN-01).
 * BLOQUEADA → LIBERADA → CONCLUIDA. Só se avança para a próxima trilha
 * quando a anterior está CONCLUIDA (respostas completas + síntese registrada).
 */
public enum JourneyTrailStatus {
    BLOQUEADA,
    LIBERADA,
    CONCLUIDA
}
