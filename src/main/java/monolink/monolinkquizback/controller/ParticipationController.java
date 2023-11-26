package monolink.monolinkquizback.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import monolink.monolinkquizback.auth.UserDetailsImpl;
import monolink.monolinkquizback.dto.ResponseDto;
import monolink.monolinkquizback.dto.ResultDto;
import monolink.monolinkquizback.service.ParticipationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Response", description = "Responses management APIs")
@Controller
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
@RequestMapping(path = "/api")
public class ParticipationController {

    @Autowired
    private ParticipationService service;

    @Operation(summary = "add Response to user", responses = {
            @ApiResponse(responseCode = "200", description = "Ok"),
    })
    @PostMapping(path = "/participation/addResponse")
    public ResponseEntity<List<ResponseDto>> addResponse(@RequestBody ResponseDto responseDto, HttpServletRequest request) {
        var auth = (Authentication) request.getUserPrincipal();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        List<ResponseDto> responseList = service.addResponse(userDetails.getId(), responseDto);
        return ResponseEntity.ok().body(responseList);
    }

    @Operation(summary = "get responses", responses = {
            @ApiResponse(responseCode = "200", description = "Ok"),
    })
    @GetMapping(path = "/participation/responses")
    public ResponseEntity<List<ResponseDto>> getResponses(HttpServletRequest request) {
        var auth = (Authentication) request.getUserPrincipal();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        List<ResponseDto> responses = service.getResponses(userDetails.getId());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "get participation result", responses = {
            @ApiResponse(responseCode = "200", description = "Ok"),
    })
    @GetMapping(path = "/participation/result")
    public ResponseEntity<ResultDto> getParticipantResult(HttpServletRequest request) {
        var auth = (Authentication) request.getUserPrincipal();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        ResultDto result = service.getParticipantResult(userDetails.getId());
        return ResponseEntity.ok(result);
    }


}
