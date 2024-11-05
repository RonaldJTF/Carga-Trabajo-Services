package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.ReglaDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.ValorVigenciaDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.VariableDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ReglaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ValorVigenciaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.VariableEntity;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import org.springframework.stereotype.Service;

@Service
public class RuleFormulaMediator {
    private final VariableDAO variableDAO;
    private final ReglaDAO reglaDAO;
    private final ValorVigenciaDAO valorVigenciaDAO;

    public RuleFormulaMediator(VariableDAO variableDAO, ReglaDAO reglaDAO, ValorVigenciaDAO valorVigenciaDAO) {
        this.variableDAO = variableDAO;
        this.reglaDAO = reglaDAO;
        this.valorVigenciaDAO = valorVigenciaDAO;
    }

    public double calcularBono(int idBono, Long idVigencia) {
        String formula = findFormulaByIdAndValidityId((long) idBono, idVigencia);
        return evaluarFormula(formula, new HashMap<>(), idVigencia); 
    }

    private double evaluarFormula(String formula, Map<Integer, Double> cache, Long idVigencia) {
        
        Pattern pattern = Pattern.compile("\\$\\[(\\d+)]");
        Matcher matcher = pattern.matcher(formula);

        while (matcher.find()) {
            int referencedId = Integer.parseInt(matcher.group(1));

            double referencedValue;
            if (cache.containsKey(referencedId)) {
                referencedValue = cache.get(referencedId);
            } else {
                referencedValue = calcularBono(referencedId, idVigencia);
                cache.put(referencedId, referencedValue);
            }

            formula = formula.replace(matcher.group(), String.valueOf(referencedValue));
        }
        Expression expression = new ExpressionBuilder(formula).build();
        return expression.evaluate();
    }

    private String findFormulaByIdAndValidityId(Long idVariable, Long idVigencia) {

        VariableEntity variable = variableDAO.findById(idVariable)
                .orElseThrow(() -> new IllegalArgumentException("Variable no encontrada con ID: " + idVariable));

        if (variable.getValor() != null && !variable.getValor().isEmpty()) {
            return variable.getValor();
        }

        VariableEntity variableConVigencia = variableDAO.findByIdAndValidityId(idVariable, idVigencia);

        if (variableConVigencia != null && variableConVigencia.getValoresVigencias() != null) {
            return variableConVigencia.getValoresVigencias()
                                    .stream()
                                    .filter(v -> v.getIdVigencia().equals(idVigencia))
                                    .map(ValorVigenciaEntity::getValor)
                                    .map(String::valueOf)
                                    .findFirst()
                                    .orElseThrow(() -> new IllegalArgumentException("No se encontró valor de vigencia para la ID proporcionada."));
        }

        throw new IllegalArgumentException("No se encontró un valor válido para la variable con ID: " + idVariable);
    }


    public String obtenerFormulaConNombres(Long idBono, Long idVigencia) {
        String formula = findFormulaByIdAndValidityId(idBono, idVigencia);
        return expandirFormulaConNombres(formula, new HashMap<>(), idVigencia); 
    }

    private String expandirFormulaConNombres(String formula, Map<Long, String> cache, Long idVigencia) {
        Pattern pattern = Pattern.compile("\\$\\[(\\d+)]");
        Matcher matcher = pattern.matcher(formula);
        
        StringBuffer formulaConNombres = new StringBuffer();
        
        while (matcher.find()) {
            Long idVariable = Long.parseLong(matcher.group(1));

            if (cache.containsKey(idVariable)) {
                matcher.appendReplacement(formulaConNombres, cache.get(idVariable));
                continue;
            }

            VariableEntity variable = variableDAO.findById(idVariable)
                .orElseThrow(() -> new IllegalArgumentException("Variable no encontrada con ID: " + idVariable));
    
            String nombreVariable = variable.getNombre();

            cache.put(idVariable, nombreVariable);
            matcher.appendReplacement(formulaConNombres, nombreVariable);
        }
    
        matcher.appendTail(formulaConNombres);
        return formulaConNombres.toString();
    }


    public void evaluarCondicionComparativa(Long reglaId, Long idVigencia) {
        ReglaEntity regla = reglaDAO.findById(reglaId)
                .orElseThrow(() -> new IllegalArgumentException("Regla no encontrada con ID: " + reglaId));
    
        String condiciones = regla.getCondiciones();
        if (condiciones == null || condiciones.isEmpty()) {
            throw new IllegalArgumentException("No existen condiciones para la regla con ID: " + reglaId);
        }
    
        boolean resultado = evaluarExpresiones(condiciones, idVigencia);
    
        if (resultado) {
            System.out.println("Todas las condiciones son verdaderas.");
        } else {
            System.out.println("Una o más condiciones son falsas.");
        }
    }
    
    private boolean evaluarExpresiones(String condiciones, Long idVigencia) {
        String[] expresiones = condiciones.split("&&");
    
        for (String expresion : expresiones) {
            if (!evaluarExpresion(expresion.trim(), idVigencia)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean evaluarExpresion(String expresion, Long idVigencia) {
        String[] partes = expresion.split("(?<=[<>!=])|(?=[<>!=])");
        double ladoIzquierdo, ladoDerecho;
    
        if (partes.length == 1) { 
            
            ladoIzquierdo = evaluarFormula(partes[0].trim(), new HashMap<>(), idVigencia);
            return ladoIzquierdo != 0; 
        } else if (partes.length == 3) {
            
            ladoIzquierdo = evaluarFormula(partes[0].trim(), new HashMap<>(), idVigencia);
            ladoDerecho = evaluarFormula(partes[2].trim(), new HashMap<>(), idVigencia);

            switch (partes[1].trim()) {
                case ">":
                    return ladoIzquierdo > ladoDerecho;
                case "<":
                    return ladoIzquierdo < ladoDerecho;
                case ">=":
                    return ladoIzquierdo >= ladoDerecho;
                case "<=":
                    return ladoIzquierdo <= ladoDerecho;
                case "==":
                    return ladoIzquierdo == ladoDerecho;
                case "!=":
                    return ladoIzquierdo != ladoDerecho;
                default:
                    throw new IllegalArgumentException("Operador desconocido: " + partes[1].trim());
            }
        } else {
            throw new IllegalArgumentException("La expresión no tiene un formato válido: " + expresion);
        }
    }

    public String obtenerCondicionesConNombres(Long reglaId, Long idVigencia) {
        ReglaEntity regla = reglaDAO.findById(reglaId)
                .orElseThrow(() -> new IllegalArgumentException("Regla no encontrada con ID: " + reglaId));
    
        String condiciones = regla.getCondiciones();
        if (condiciones == null || condiciones.isEmpty()) {
            throw new IllegalArgumentException("No existen condiciones para la regla con ID: " + reglaId);
        }

        String[] expresiones = condiciones.split("&&");

        StringBuilder condicionesConNombres = new StringBuilder();
        for (int i = 0; i < expresiones.length; i++) {
            String expresionConNombres = expandirFormulaConNombres(expresiones[i].trim(), new HashMap<>(), idVigencia);
            condicionesConNombres.append(expresionConNombres);
    
            if (i < expresiones.length - 1) {
                condicionesConNombres.append(" && ");
            }
        }
    
        return condicionesConNombres.toString();
    }



}