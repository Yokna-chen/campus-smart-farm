package com.campus.farm.irrigation;

import com.campus.farm.auth.User;
import com.campus.farm.irrigation.dto.IrrigationCommandRequest;
import com.campus.farm.irrigation.dto.IrrigationCommandResponse;
import com.campus.farm.irrigation.dto.IrrigationZoneResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class IrrigationController {
  private final IrrigationService irrigation;
  private final IrrigationZoneRepository zones;
  private final IrrigationCommandPort port;

  public IrrigationController(IrrigationService irrigation, IrrigationZoneRepository zones,
      IrrigationCommandPort port) {
    this.irrigation = irrigation; this.zones = zones; this.port = port;
  }

  @GetMapping("/zones")
  public List<IrrigationZoneResponse> zones() {
    return zones.findAll().stream().map(zone -> new IrrigationZoneResponse(zone,
        port.readStatus(zone.getZoneId()))).collect(Collectors.toList());
  }

  @PostMapping("/irrigation/{zoneId}/{action}")
  public IrrigationCommandResponse command(@PathVariable String zoneId, @PathVariable String action,
      @RequestBody(required = false) IrrigationCommandRequest request, Authentication authentication) {
    if (request == null) throw new IrrigationBadRequestException("request body is required");
    User user = (User) authentication.getDetails();
    return new IrrigationCommandResponse(irrigation.command(user.getUsername(), user.getRole(), zoneId,
        action, request.getDurationMinutes(), request.getReason()));
  }

}
