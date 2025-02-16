package com.rently.rentlyAPI.controller;

import com.rently.rentlyAPI.dto.*;
import com.rently.rentlyAPI.services.CompanyAdminService;
import com.rently.rentlyAPI.services.S3Service;
import com.rently.rentlyAPI.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/company-admin")
@RequiredArgsConstructor
public class CompanyAdminController {
    private final UserService userService;
    private final CompanyAdminService companyAdminService;
    private final S3Service s3Service;

    @PostMapping(path = "/create/building")
    public ResponseEntity<BuildingDto> createBuilding(@RequestHeader("Authorization") String token, @RequestBody BuildingDto buildingDto) {
        return ResponseEntity.ok(companyAdminService.createBuildingAndLinkToCompany(token, buildingDto));
    }

    @GetMapping(path = "/buildings/id={buildingId}")
    public ResponseEntity<BuildingDto> getBuildingById(@RequestHeader("Authorization") String token, @PathVariable(name = "buildingId") Integer buildingId) {
        return ResponseEntity.ok(companyAdminService.getBuildingById(token, buildingId));
    }

    @GetMapping(path = "/buildings/name={buildingName}")
    public ResponseEntity<BuildingDto> getBuildingByName(@RequestHeader("Authorization") String token, @PathVariable(name = "buildingName") String buildingName) {
        return ResponseEntity.ok(companyAdminService.getBuildingByName(token, buildingName));
    }

    //company admin can see all his buildings
    @GetMapping(path = "/buildings")
    public ResponseEntity<List<BuildingDto>> getAllBuildings(@RequestHeader("Authorization") String token) {
        // token.substring(7) To remove the Bearer prefix from the token
        Integer companyId = companyAdminService.findCompanyAdminEntityByToken(token).getCompany().getId();
        return ResponseEntity.ok(companyAdminService.getAllBuildingsByCompanyId(companyId));
    }
    // Condo
    @PostMapping(path = "/create/condo")
    public ResponseEntity<CondoDto> createCondo(@RequestBody CondoDto condoDto) {
        return ResponseEntity.ok(companyAdminService.createCondoAndLinkToBuilding(condoDto));
    }
    
    @GetMapping(path = "/condos/all")
    public ResponseEntity<List<CondoDto>> getAllCondos() {
        return ResponseEntity.ok(companyAdminService.getAllCondos());
    }
    
    @GetMapping(path = "/condos/buildingId={buildingId}")
    public ResponseEntity<List<CondoDto>> getAllCondosByBuildingId(@PathVariable(name = "buildingId") Integer buildingId) {
        return ResponseEntity.ok(companyAdminService.getAllCondosByBuildingId(buildingId));
    }
    
    @DeleteMapping(path = "/condos/delete/condoId={condoId}")
    public ResponseEntity<String> deleteCondoById(@PathVariable(name = "condoId") Integer condoId) {
        String confirmation = companyAdminService.deleteCondoById(condoId);
        return ResponseEntity.ok(confirmation);
    }
    
    @PostMapping(path = "/generate-key-and-create-housing-contract-for-condo")
    public ResponseEntity<String> sendKeyToUser(@RequestBody CombinedRequestDto combinedRequestDto) {
        return ResponseEntity.ok(companyAdminService.generateKeyForCondoAndCreateHousingContract(combinedRequestDto.getRegistrationKeyRequestDto(), combinedRequestDto.getHousingContractDto()));
    }
    
    @PostMapping(path = "/send-key-to-future-occupant")
    public ResponseEntity<String> sendKeyToFutureOccupant(@RequestBody EmailDto EmailDto) {
        return ResponseEntity.ok(companyAdminService.sendKeyAndHousingContractToFutureOccupant(EmailDto));
    }
    
    @PostMapping("/upload/condo-file/condo={condoId}")
    public ResponseEntity<?> uploadCondoFile(
        @RequestParam("file") MultipartFile multipartFile,
        @RequestParam("description") String description,
        @PathVariable(name = "condoId") Integer condoId
    ) throws Exception {
        s3Service.uploadCondoFile(multipartFile, description, condoId);
        return ResponseEntity.ok().body("Condo's file uploaded successfully.");
    }
    
    // TODO: get all condo file by condo id
    // TODO: get condo file by id
    

    //common facilities
    @PostMapping(path = "/create/common-facility")
    public ResponseEntity<CommonFacilityDto> createCommonFacility(@RequestBody CommonFacilityDto commonFacilityDto) {
        return ResponseEntity.ok(companyAdminService.createCommonFacilityAndLinkToBuilding(commonFacilityDto));
    }

    @GetMapping(path = "/common-facilities/id={commonFacilityId}")
    public ResponseEntity<CommonFacilityDto> getCommonFacilityById(@PathVariable(name = "commonFacilityId") Integer commonFacilityId) {
        return ResponseEntity.ok(companyAdminService.getCommonFacilityById(commonFacilityId));
    }

    @GetMapping(path = "/common-facilities")
    public ResponseEntity<List<CommonFacilityDto>> getAllCommonFacilities() {
        // token.substring(7) To remove the Bearer prefix from the token
        return ResponseEntity.ok(companyAdminService.getAllCommonFacilities());
    }

    @GetMapping(path = "/common-facilities/building={buildingId}")
    public ResponseEntity<List<CommonFacilityDto>> getAllCommonFacilitiesForABuilding(@PathVariable(name = "buildingId") Integer buildingId) {
        // token.substring(7) To remove the Bearer prefix from the token
        return ResponseEntity.ok(companyAdminService.getAllCommonFacilitiesByBuildingId(buildingId));
    }

    @DeleteMapping(path = "/delete/common-facilities/id={commonFacilityId}")
    public ResponseEntity<String> deleteCommonFacilityById(@PathVariable(name = "commonFacilityId") Integer commonFacilityId) {
        companyAdminService.deleteCommonFacilityById(commonFacilityId);
        return ResponseEntity.ok("Common Facility with id: " + commonFacilityId + " has been deleted");
    }


    // Employees
    @PostMapping(path = "/create/employee")
    public ResponseEntity<EmployeeDto> registerEmployee(@RequestBody EmployeeDto employeeDto) {
        return ResponseEntity.ok(userService.registerEmployee(employeeDto));
    }

    @PatchMapping(path = "/update/employee")
    public ResponseEntity<EmployeeDto> updateEmployee(@RequestBody EmployeeDto employeeDto) {
        return ResponseEntity.ok(companyAdminService.updateEmployee(employeeDto));
    }

    @GetMapping(path = "/employees")
    public ResponseEntity<List<EmployeeDto>> getAllEmployees(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(companyAdminService.getAllEmployees(token));
    }

    @GetMapping(path = "/employees/type={employeeType}/building={buildingId}")
    public ResponseEntity<List<EmployeeDto>> getAllEmployeesByTypeAndBuilding(@RequestHeader("Authorization") String token, @PathVariable String employeeType, @PathVariable Integer buildingId) {
        return ResponseEntity.ok(companyAdminService.getAllEmployeesByTypeAndBuilding(token, employeeType, buildingId));
    }

    @DeleteMapping(path = "/delete/employee/id={id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable("id") Integer id) {
        userService.deleteEmployee(id);
        return ResponseEntity.ok("Employee deleted successfully");
    }

    @PostMapping(path = "/create/employment-contract")
    public ResponseEntity<EmploymentContractDto> createEmploymentContract(@RequestBody EmploymentContractDto employmentContractDto) {
        return ResponseEntity.ok(companyAdminService.createEmploymentContract(employmentContractDto));
    }


    // get all assignments
    @GetMapping(path = "/assignments")
    public ResponseEntity<List<EmployeeAssignmentDto>> getAllEmployeeAssignments(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(companyAdminService.getAllEmployeeAssignments(token));
    }

    @GetMapping(path = "/assignments/unassigned")
    public ResponseEntity<List<EmployeeAssignmentDto>> getAllUnassignedEmployeeAssignments(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(companyAdminService.getAllUnassignedEmployeeAssignments(token));
    }

    @PatchMapping(path = "/assignments/assign/employee={employeeId}/assignment={assignmentId}")
    public ResponseEntity<EmployeeAssignmentDto> assignEmployeeToAssignment(@PathVariable Integer employeeId, @PathVariable Integer assignmentId) {
        return ResponseEntity.ok(companyAdminService.assignEmployeeToAssignment(employeeId, assignmentId));
    }


}
