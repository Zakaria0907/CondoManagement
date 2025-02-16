package com.rently.rentlyAPI.services.impl;

import com.rently.rentlyAPI.dto.AssignmentUpdateDto;
import com.rently.rentlyAPI.dto.EmployeeAssignmentDto;
import com.rently.rentlyAPI.dto.EmployeeDto;
import com.rently.rentlyAPI.entity.Company;
import com.rently.rentlyAPI.entity.EmploymentContract;
import com.rently.rentlyAPI.entity.enums.WorkType;
import com.rently.rentlyAPI.entity.user.Employee;
import com.rently.rentlyAPI.exceptions.AuthenticationException;
import com.rently.rentlyAPI.repository.EmployeeRepository;
import com.rently.rentlyAPI.repository.EmploymentContractRepository;
import com.rently.rentlyAPI.services.CompanyService;
import com.rently.rentlyAPI.services.EmployeeAssignmentService;
import com.rently.rentlyAPI.services.EmployeeService;
import com.rently.rentlyAPI.utils.JwtUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final CompanyService companyService;
    private final EmploymentContractRepository employmentContractRepository;
    private final EmployeeAssignmentService employeeAssignmentService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public EmployeeDto registerEmployee(EmployeeDto employeeDto) {
        if (employeeDto.getPassword() != null) {
            // Encode the password if provided (null with google for example)
            String encodedPassword = passwordEncoder.encode(employeeDto.getPassword());
            employeeDto.setPassword(encodedPassword);
        }
        // Retrieve company with the given ID
        Company companyToLink = companyService.findCompanyEntityById(employeeDto.getCompanyId());

        // Create the employee
        Employee employeeToSave = EmployeeDto.toEntity(employeeDto);

        // Link the employee to the company
        employeeToSave.setCompany(companyToLink);

        // Save the employee
        Employee savedEmployee = employeeRepository.save(employeeToSave);

        // Return the employee dto
        return EmployeeDto.fromEntity(savedEmployee);
    }

    @Override
    public List<EmployeeDto> getAllEmployeesByCompanyId(Integer companyId) {
        List<Employee> employees = employeeRepository.findAllByCompanyId(companyId);
        return employees.stream()
                .map(EmployeeDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Employee> findByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    @Override
    public Employee findById(Integer employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee with id: " + employeeId + " not found"));
    }

    @Override
    public Employee findEmployeeEntityByToken(String token) {
        String tokenWithoutBearer = token.substring(7);
        String email = jwtUtils.extractUsername(tokenWithoutBearer);
        Optional<Employee> employee = findByEmail(email);
        if (employee.isPresent()) {
            return employee.get();
        }
        throw new AuthenticationException("Employee with email " + email + " not found");
    }

    @Override
    public void deleteEmployee(Integer id) {
        findById(id);
        employeeRepository.deleteById(id);
    }

    @Override
    public EmployeeDto updateEmployee(EmployeeDto employeeDto) {
        // Find the Employee Entity by its ID
        Employee employeeToUpdate = findById(employeeDto.getId());

        // Update Employee details if present
        if (employeeDto.getFirstName() != null && !employeeDto.getFirstName().isEmpty()) {
            employeeToUpdate.setFirstName(employeeDto.getFirstName());
        }
        if (employeeDto.getLastName() != null && !employeeDto.getLastName().isEmpty()) {
            employeeToUpdate.setLastName(employeeDto.getLastName());
        }
        if (employeeDto.getEmployeeType() != null && !employeeDto.getEmployeeType().isEmpty()) {
            employeeToUpdate.setEmployeeType(WorkType.valueOf(employeeDto.getEmployeeType()));
        }

        // Save the updated employee
        Employee updatedEmployee = employeeRepository.save(employeeToUpdate);

        return EmployeeDto.fromEntity(updatedEmployee);
    }

    @Override
    public List<EmployeeDto> getAllEmployeesByTypeAndBuilding(Integer companyId, String employeeType, Integer buildingId) {
        List<EmploymentContract> contracts = employmentContractRepository.findByCompanyIdAndBuildingId(companyId, buildingId);
        if (contracts.isEmpty()) {
            System.out.println(contracts);
            throw new EntityNotFoundException("No employees found for building: " + buildingId);
        }
        List<EmployeeDto> employees = contracts.stream()
                .map(EmploymentContract::getEmployee)
                .filter(employee -> employee.getEmployeeType().equals(WorkType.valueOf(employeeType)))
                .map(EmployeeDto::fromEntity)
                .collect(Collectors.toList());
        if (employees.isEmpty()) {
            throw new EntityNotFoundException("No employees found for building: " + buildingId + " with type: " + employeeType);
        }

        return employees;
    }

    @Override
    public List<EmployeeAssignmentDto> getAllEmployeeAssignmentsByCompanyId(Integer companyId) {
        return employeeAssignmentService.getAllEmployeeAssignmentsByCompanyId(companyId);
    }

    @Override
    public List<EmployeeAssignmentDto> getAllUnassignedEmployeeAssignmentsByCompanyId(Integer companyId) {
        return employeeAssignmentService.getAllUnassignedEmployeeAssignmentsByCompanyId(companyId);
    }

    @Override
    public List<EmployeeAssignmentDto> getAllAssignments(String token) {
        Employee employee = findEmployeeEntityByToken(token);
        return employeeAssignmentService.getAllAssignments(employee.getId());
    }

    @Override
    public EmployeeAssignmentDto getAssignmentById(String token, Integer assignmentId) {
        Employee employee = findEmployeeEntityByToken(token);
        return employeeAssignmentService.getAssignmentByEmployeeIdAndAssignmentId(employee.getId(), assignmentId);
    }

    @Override
    public AssignmentUpdateDto updateAssignmentStatus(String token, AssignmentUpdateDto assignmentUpdateDto, Integer id) {
        EmployeeAssignmentDto assignmentDto = getAssignmentById(token, id);
        return employeeAssignmentService.updateAssignmentStatus(assignmentDto.getId(), assignmentUpdateDto);
    }


}
