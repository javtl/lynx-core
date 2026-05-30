package com.nominal.lynx.infrastructure.input.rest;

import com.nominal.lynx.application.dto.CreateBatchRequest;
import com.nominal.lynx.application.dto.BatchResponse;
import com.nominal.lynx.application.dto.ChangeStatusRequest; // ◄ NUEVO IMPORT LYNX-06
import com.nominal.lynx.application.port.in.CreateBatchUseCase;
import com.nominal.lynx.application.port.in.GetBatchUseCase;
import com.nominal.lynx.application.port.in.ChangeStatusUseCase; // ◄ NUEVO IMPORT LYNX-06
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * BatchController — Expone operaciones CRUD de lotes vía REST 🌐
 *
 * Responsabilidades:
 * 1. Recibir HTTP (JSON)
 * 2. Validar con Jakarta Validation (@Valid)
 * 3. Delegar al Use Case
 * 4. Devolver HTTP (JSON)
 */
@RestController
@RequestMapping("/api/v1/batches")
@Tag(name = "Batches", description = "Operaciones CRUD de lotes")
public class BatchController {

    private final CreateBatchUseCase createBatchUseCase;
    private final GetBatchUseCase getBatchUseCase;
    private final ChangeStatusUseCase changeStatusUseCase; // ◄ AGREGADO PARA LYNX-06

    // Constructor modificado para inyectar el nuevo caso de uso que configuramos en UseCaseBeans
    public BatchController(CreateBatchUseCase createBatchUseCase,
                           GetBatchUseCase getBatchUseCase,
                           ChangeStatusUseCase changeStatusUseCase) { // ◄ AGREGADO PARA LYNX-06
        this.createBatchUseCase = createBatchUseCase;
        this.getBatchUseCase = getBatchUseCase;
        this.changeStatusUseCase = changeStatusUseCase; // ◄ AGREGADO PARA LYNX-06
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo lote", description = "Crea un lote en estado DRAFT")
    public ResponseEntity<BatchResponse> createBatch(@Valid @RequestBody CreateBatchRequest request) {
        BatchResponse response = createBatchUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un lote", description = "Devuelve los detalles de un lote")
    public ResponseEntity<BatchResponse> getBatch(@PathVariable UUID id) {
        BatchResponse response = getBatchUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    // 🔧 NUEVO ENDPOINT AGREGADO PARA LYNX-06: Modificación de estado con respuesta semántica 204
    @PatchMapping("/{id}/status")
    @Operation(summary = "Cambiar estado de un lote", description = "Transiciona el estado con validación")
    public ResponseEntity<Void> changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeStatusRequest request) {

        // El ID de usuario se genera de forma temporal; se conectará al JWT en el Sprint 3
        UUID temporaryUserId = UUID.randomUUID();

        // Invocación exacta según la firma real void execute(UUID, String, UUID, String)
        changeStatusUseCase.execute(id, request.newStatus(), temporaryUserId, request.reason());

        return ResponseEntity.noContent().build(); // Devuelve HTTP 204 No Content
    }
}