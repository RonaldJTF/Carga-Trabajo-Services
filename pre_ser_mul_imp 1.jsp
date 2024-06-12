<!-- 
	ARCHIVO: servicios/pre_ser_mul_mod.jsp
	@AUTOR: Grupo de desarrollo Plataforma - Universidad de Pamplona
	FECHA DE CREACION: 23-SEPTIEMBRE-2005  
	APLICACIÓN: Prestamos - Academusoft 3.2
	CASO DE USO: -
	DOCUMENTO: GSE-17
 -->

<%@ page import ="prestamo.valueobject.*"%>
<%@ page import="co.edu.unipamplona.plataforma.comunes.*" %>
<%@ page import ="java.util.*"%>
<%@ page import ="general.valueobject.BuscaPersonaVO"%>
<%@ page import ="general.valueobject.PersonaNaturalGeneralVO"%>
<%@ page import ="prestamo.metodos.*"%>
<%@ page import ="prestamo.fachada.*"%>
<%@ page import ="co.edu.unipamplona.plataforma.constantes.*" %>
<%@ page import="co.edu.unipamplona.plataforma.metodos.*" %>

<!--Andres Villamizar Vera  08-08-2011-->
<%@ page import="javax.naming.*" %>
<%@ page import ="prestamo.valueobject.*"%>
<%@ page import ="java.util.ArrayList"%>
<%@ page import ="general.valueobject.BuscaPersonaVO"%>
<%@ page import ="general.valueobject.PersonaNaturalGeneralVO"%>
<%@ page import ="prestamo.metodos.*"%>
<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %> 
<%@ page import="co.edu.unipamplona.plataforma.metodos.*" %>


<%//@ page import="academico.fachada.*" %>
<%//@ page import="academicob.fachada.*" %>
<%//@ page import="academico.valueobject.*" %>
<%//@ page import="academicob.fachada.*" %>
<%//@ page import="academicob.estrategia.*" %>

<script language="javascript">
alert( document.getElementById("nuevaLiquidacion").checked) ;
</script>

<% 
	String  estiloApl = "001";
	MiUsuarioVO miUsuarioVO = (MiUsuarioVO)session.getAttribute("miUsuarioVO");
	boolean validarSession = ValidarSession.validarUsuarioGeneral(miUsuarioVO, "302"); 
	if(!validarSession) {
		response.sendRedirect("../fuera.jsp?control="+String.valueOf(Math.random()));
	}	else {
		estiloApl = (String)miUsuarioVO.getEstilo();
	
%>

<script language="javascript"  src="../jScripts/ventana.js" ></script>
<script language="javascript" src="../jScripts/validaciones.js"></script>

<%
  InitialContext initCtx = new InitialContext();
  Boolean multabibliotecaGenLiquidacion = new Boolean("false");
  multabibliotecaGenLiquidacion = (Boolean)initCtx.lookup("java:comp/env/liquidaMultaBibliotecaUP");
  
   Boolean variableAmbienteIES = new Boolean("false");
   variableAmbienteIES = (Boolean) initCtx.lookup("java:comp/env/variableAmbienteIES");

  
  
  
  //variables comunes
    PersonaNaturalGeneralVO personaNaturalGeneralVO=(PersonaNaturalGeneralVO)session.getAttribute("personaNaturalGeneralVO");
	PrestamoMultaVO prestamoMultaVO = (PrestamoMultaVO)session.getAttribute("prestamoMultaVO");
	
	UnpaPersonaGeneralVO usuarioUnpaUnidadPrestadoraVO = (UnpaPersonaGeneralVO)session.getAttribute("usuarioUnpaUnidadPrestadoraVO");
	UnidadPrestadoraVO usuarioUnidadPrestadoraVO = null;
	
	if(usuarioUnpaUnidadPrestadoraVO!=null){
		usuarioUnidadPrestadoraVO = (UnidadPrestadoraVO)usuarioUnpaUnidadPrestadoraVO.getUnidadPrestadora();
	}

%>



<%

if(prestamoMultaVO!=null){
	String operacion = (String)request.getParameter("liquidacion");

if(multabibliotecaGenLiquidacion&& !prestamoMultaVO.getEstado().equals("X") ){  //inicio  variable java:comp/env/liquidaMultaBibliotecaUP estado true
	
	System.out.println("variable estado liquidaMultaBibliotecaUP [true]");
		
	
	String archivo="";
	FachadaModuloPrestamo fachadaprestamo=new  FachadaModuloPrestamo();
	personaNaturalGeneralVO.setPege_id(personaNaturalGeneralVO.getIdPersona());
	personaNaturalGeneralVO.setRegistradoPor(miUsuarioVO.getCodigoUsuario());
	
	int puerto = request.getServerPort();
	String PathContexto = request.getContextPath();
	String esquema = request.getScheme();
	String nombreServidor = request.getServerName();	
 	//String puertoServidor = String.valueOf(puerto);
	nombreServidor = "academico.jdc.edu.co";
	puerto = 80;
	PathContexto = "/lectorBarras";
	esquema = "http";

	System.out.println("\n\n-----------------------------\n");
	System.out.println("puerto = " + puerto);
	System.out.println("PathContexto = " + PathContexto);
	System.out.println("esquema = " + esquema);
	System.out.println("nombreServidor = " + nombreServidor);
	System.out.println("\n-----------------------------\n\n");
	
	ArrayList datosserver=new ArrayList(); 
	datosserver.add(puerto);
	datosserver.add(PathContexto);
	datosserver.add(esquema);
	datosserver.add(nombreServidor);
	//Se agrega campo para detrerminar si se va a generar un anueva liquidacion
	if(operacion!=null){
		datosserver.add("nueva");
	}
	
	//Como armar el arreglo para ip externa
	//ejemplo: http://academusoft.unipamplona.edu.co/unipamplona/academusoft/academico
	//ip y puerto del dominio anterior : 190.90.230.4:80
	//nombreServidor = "190.90.230.4";
    //puerto = 80;
    //PathContexto = "/unipamplona/academusoft/academico";
	
	
	archivo=(String)fachadaprestamo.generarformatoliquidacion(prestamoMultaVO,personaNaturalGeneralVO, datosserver,application.getRealPath("") );
	
	System.out.println("archivo=>"+archivo);
	
	if(variableAmbienteIES&&!Metodos.isNullOrEmptyString(archivo)){
		response.sendRedirect("../resultadosPDF/liquidaciones_tipo_cobro/pdf/"+archivo);
	} else if( !Metodos.isNullOrEmptyString(archivo)){
		response.sendRedirect("../resultadosPDF/liquidaciones_up/pdf/"+archivo);
	}

} //fin de validacion de pagina para  cuando variable web.xml esta ACTIVA(true)


else {//inicio  variable java:comp/env/liquidaMultaBibliotecaUP estado false
	
	
	if(multabibliotecaGenLiquidacion&& prestamoMultaVO.getEstado().equals("X")){
		%>
		<script language="javascript">
		alert("La multa tiene estado ANULADA, no se genera liquidación");
        //window.close();
	    </script>
        
        <%
		System.out.println("cumple la condicion");

		}
	
	
	
	System.out.println("variable estado liquidaMultaBibliotecaUP [false]");


	ElementoPrestamoVO elementoPrestamoVO = null;
	boolean permitible = false;
	if(prestamoMultaVO!=null){
		elementoPrestamoVO = (ElementoPrestamoVO)prestamoMultaVO.getElementoPrestamo();
		
		if(prestamoMultaVO.getIntegrado().equals("N")){
			permitible = true;
		}
	}

	ElementoEstadoVO elementoEstadoVO = null;
	ArrayList listaElementoComponente = null;
	ColeccionVO coleccionVO = null;
	if(elementoPrestamoVO!=null){
		elementoEstadoVO = (ElementoEstadoVO)elementoPrestamoVO.getElementoEstado();
		listaElementoComponente = (ArrayList)elementoPrestamoVO.getListaElementoComponente();
		coleccionVO = (ColeccionVO)elementoPrestamoVO.getColeccion();
	}
	
	Calendar fecha = Calendar.getInstance();
	String fechaReporte = Format.formatCalendar(fecha, Constantes.DATEFORMAT_D_M_Y_H_M_S);

%>


<script language="javascript">
function registrar(frm,vinculo){
<%if(!permitible){%>
	alert("La multa no puede ser gestionada por esta funcionalidad.");
<%}else{%>
	
	if(confirm ("¿Está seguro que desea modificar la multa?")){
	  frm.action = vinculo;
	  frm.submit();
	}
<%}%>	
}
</script> 
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title>Servicios Acad&eacute;micos - Academusoft 3.2</title>
<link href="../css/estilo.css" rel="stylesheet" type="text/css">
</head>
<body>
<form name="form1" method="post">
<table width="773" border="0" align="center" cellpadding="0" cellspacing="5" class="fondo_tabla">
  <tr> 
    <td align="center" valign="top" class="fondo_celda_3"> <table width="100%" border="0" cellspacing="0" cellpadding="0">
        <tr> 
          <td height="55"> <table width="100%" border="0" cellspacing="0" cellpadding="0">
              <tr class="fondo_celda_2"> 
                <td width="476" height="15" colspan="3"><table width="100%" border="0" cellspacing="0" cellpadding="0">
                    <tr>
                      <td width="50%" align="left" class="text_negro"><%= miUsuarioVO != null ? Metodos.printString(miUsuarioVO.getNombre()) : "" %></td>
                      <td width="50%" align="right" class="text_mensaje"><span class="text_negro"><%= usuarioUnidadPrestadoraVO != null ? Metodos.printString(usuarioUnidadPrestadoraVO.getNombreLargo()) : "[TODAS LAS UNIDADES]" %></span></td>
                    </tr>
                  </table> </td>
              </tr>
              
            </table></td>
        </tr>
      </table>
      <table width="100%" border="0" cellpadding="0" cellspacing="5" class="fondo_celda_3">
        <tr align="center" valign="top" class="fondo_celda_3"> 
          <td width="100%"><table border="0" align="center" cellpadding="2" cellspacing="0" class="fondo_celda_1" width="700">
              <tr> 
                <td valign="top" > <table width="100%" border="0" align="center" cellpadding="0" cellspacing="1" class="fondo_celda_3">
                  <tr align="center" valign="middle" class="fondo_celda_2">
                    <td height="15"   align="left" class="text_negro"> Fecha del reporte <%= fechaReporte != null ? Metodos.printString(fechaReporte) : "" %> </td>
                  </tr>
                    <tr class="fondo_celda_1"> 
                      <td height="20" class="text_blanco" align="center" >Ver Multa</td>
                    </tr>
                    <tr> 
                      <td align="center" valign="top"> 
					    <%if(!permitible){%>
                        <table width="100%" border="0" cellspacing="1" cellpadding="2">
                          <tr class="fondo_celda_2">
                            <td colspan="2" align="center" class="text_negro">Alerta </td>
                          </tr>
                          <tr align="left" valign="middle">
                            <td width="15%" align="center" class="text_mensaje"><img src="../images/images_<%=estiloApl%>/alr_a.gif" width="15" height="15"></td>
                            <td width="85%" align="center" class="text_mensaje">La multa no puede ser gestionada por esta funcionalidad</td>
                          </tr>
                        </table>
                        <%}%>
                      		<table width="100%" border="0" cellspacing="0" cellpadding="0">
                              <tr align="left" valign="middle">
                                <td width="19%" height="50" align="center" class="text_negro"><img src="../servlet/foto?idPersona=<%=personaNaturalGeneralVO!=null?Metodos.printString(personaNaturalGeneralVO.getIdPersona()):""%>" width="100" height="120" hspace="5" vspace="5" border="1"/> </td>
                                <td width="81%" align="center" valign="top" class="text_negro"><table width="100%" border="0" cellspacing="1" cellpadding="2">
                                    <tr align="left" valign="middle"class="fondo_celda_2" >
                                      <td width="39%" align="center" class="text_negro">Unidad</td>
                                    </tr>
                                    <tr align="left" valign="middle">
                                      <td align="center" class="text_negro"><span class="text_mensaje"><%= usuarioUnidadPrestadoraVO != null ? Metodos.printString(usuarioUnidadPrestadoraVO.getNombreLargo()) : "[TODAS LAS UNIDADES]" %></span></td>
                                    </tr>
                                  </table>
                                    <table width="100%" border="0" cellspacing="1" cellpadding="2">
                                      <tr align="left" valign="middle"class="fondo_celda_2" >
                                        <td width="39%" align="center" class="text_negro">Nombres y apellidos</td>
                                      </tr>
                                      <tr align="left" valign="middle">
                                        <td align="center" class="text_negro"><%=personaNaturalGeneralVO!=null?Metodos.printString(personaNaturalGeneralVO.getNombre()):""%></td>
                                      </tr>
                                    </table>
                                    <table width="100%" border="0" cellspacing="1" cellpadding="2">
                                      <tr class="fondo_celda_2">
                                        <td width="20%" align="center" class="text_negro">Identificaci&oacute;n</td>
                                        <td width="19%" align="center" class="text_negro">Tipo</td>
                                      </tr>
                                      <tr align="left" valign="middle">
                                        <td align="center" class="text_negro"><%=personaNaturalGeneralVO!=null?Metodos.printString(personaNaturalGeneralVO.getDocumentoIdentidad()):""%></td>
                                        <td align="center" class="text_negro"><%=personaNaturalGeneralVO!=null?Metodos.printString(personaNaturalGeneralVO.getAbreviaturaTipoDocumento()):""%></td>
                                      </tr>
                                  </table></td>
                              </tr>
                            </table>
                      		<table width="100%" border="0" cellspacing="1" cellpadding="2">
                              <tr align="left" valign="middle" class="fondo_celda_2">
                                <td width="23%" align="center" class="text_negro">Valor</td>
                                <td width="36%" align="center" class="text_negro">Tiempos</td>
                                <td width="18%" align="center" class="text_negro">Valor unidad</td>
								<td width="23%" align="center" class="text_negro">Estado</td>
                              </tr>
                              <tr align="left" valign="middle" class="fondo_celda_3">
                                <td width="23%" align="center" class="text_negro"><%=prestamoMultaVO!=null?Metodos.formatToMoney(prestamoMultaVO.getValor()):""%></td>
                                <td width="36%" align="center" class="text_negro"><%=prestamoMultaVO!=null?Metodos.formatToNumero(prestamoMultaVO.getCantidad()):""%> - <%=prestamoMultaVO!=null?MetodosPrestamo.printParametrizacionMedida(prestamoMultaVO.getUnidadMedida()):""%></td>
                                <td width="18%" align="center" class="text_negro"><%=prestamoMultaVO!=null?Metodos.formatToMoney(prestamoMultaVO.getValorUnidad()):""%></td>
								<td width="23%" align="center" class="text_negro">
								
								<%=prestamoMultaVO!=null?MetodosPrestamo.printEstadoPrestamoMulta(prestamoMultaVO.getEstado()):""%>								</td>
                              </tr>
                            </table>
							<table width="100%" border="0" cellspacing="1" cellpadding="2">
                              <tr class="fondo_celda_2">
                                <td width="61%" align="center" class="text_negro">Observacion</td>
                              </tr>
							  
                              <tr align="left" valign="middle">
                                <td width="61%" align="center" class="text_negro"><%=prestamoMultaVO!=null?Metodos.printString(prestamoMultaVO.getObservacion()):""%></td>
                              </tr>
                            </table>
							  <table width="100%" border="0" cellspacing="1" cellpadding="2">
                                <tr class="fondo_celda_2">
                                  <td colspan="3" align="center" class="text_negro"><%=MetodosEtiqueta.printEtiqueta(ConstantesEtiqueta.ET_G_ELEMENTO, session.getAttribute("listaValorUnpaEtiqueta"))%> en multa </td>
                                </tr>
                                <tr class="fondo_celda_4">
                                  <td align="center" class="text_negro"><%=MetodosEtiqueta.printEtiqueta(ConstantesEtiqueta.ET_N_INTERNO, session.getAttribute("listaValorUnpaEtiqueta"))%></td>
                                  <td align="center" class="text_negro"><%=MetodosEtiqueta.printEtiqueta(ConstantesEtiqueta.ET_T_CODIGO, session.getAttribute("listaValorUnpaEtiqueta"))%></td>
                                  <td align="center" class="text_negro"><%=MetodosEtiqueta.printEtiqueta(ConstantesEtiqueta.ET_T_ELEMENTO, session.getAttribute("listaValorUnpaEtiqueta"))%></td>
                                </tr>
                                <tr align="left" valign="middle">
                                  <td width="23%" align="center" class="text_negro"><%=elementoPrestamoVO!=null?Metodos.printString(elementoPrestamoVO.getInterno()):""%></td>
                                  <td width="26%" align="center" class="text_negro"><%=elementoPrestamoVO!=null?Metodos.printString(elementoPrestamoVO.getCodigo()):""%></td>
                                  <td width="51%" align="center" class="text_negro"><%=elementoPrestamoVO!=null?Metodos.printString(elementoPrestamoVO.getDescripcion()):""%></td>
                                </tr>
                              </table>					  </td>
                    </tr>
                  </table></td>
              </tr>
              <tr class="fondo_celda_3"> 
                <td align="right" valign="top" >&nbsp;                </td>
              </tr>
            </table></td>
        </tr>
      </table></td>
  </tr>
</table>
</form>
</body>
</html>
<script language="javascript">

 window.print();
 setTimeout("window.close()",1000);
</script>
<!-- 
	13-SEPTIEMBRE-2005   @: HILLMER ALIRIO CHONA MORENO	 &: INTERFAZ
 -->
   <!--       --------------------
		modificacion  25-07-2008  YANIS STANLEY PEREZ ARDILA  -->


<%
}//fin de validacion de pagina para  cuando variable web.xml esta INACTIVA(false)
%>

<%
}//fin de prestamomulta!=null
%>


<% } %>