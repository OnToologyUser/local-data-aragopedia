##### �Se paginan los valores de las codelist si accedo con este tipo de query http://opendata.aragon.es/kos/iaest/ano-de-construccion?

> No se pagina el resultado ya que los codelist solo va a devolver un resultado y a trav�s de hasTopConcept se puede obtener sus posibles valores.

##### �Es siempre las entidades territoriales refArea en el dsd?

>  Si, las entidades territoriales son siempre refArea en el dsd
	
##### �Puedo considerar refPeriod (si aparece) como serie temporal? �Variar� para ese mismo informe o no hay series temporales?

> Si, refPeriod es una serie temporal, pero en los datos casi nunca aparece y se est� agregando manualmente, podr� variar cuando se introduzca en los datos originales.

##### �Como puedo obtener las entidades territoriales que cumplen m�s de un criterios?

> Con una query en el punto sparql, en http://opendata.aragon.es:8890/sparql

##### En una query sparql filtrando por una dimension del cubo de datos 01-010002TC me devuelve 6 resultados, cuando esperaba solo uno �Es normal?

> Si es normal, para el csv que comentas, es un cubo con las siguientes dimensiones:
> 
> * Comarca nombre	
> * REGTENEN ORDEN	
> * R�gimen de tenencia (agregado)	
> * R�gimen de tenencia (detalle)	
> 
> Y la siguiente medida:
>
> * N�mero hogares
> 
> Para obtener solo un resultado tendr�as que filtrar por m�s dimensiones. Por ejemplo filtrar por R�gimen de tenencia (agregado) y R�gimen de tenencia (detalle).
> 
> Para saber los posibles valores de esas dimensiones entrar�as en las siguientes URL:
> 
> http://opendata.aragon.es/kos/iaest/regimen-de-tenencia-agregado
> http://opendata.aragon.es/kos/iaest/regimen-de-tenencia-detalle

##### �en todos los cubos tengo que filtrar por todas las dimensiones que tenga?

> Simplificando si. Pero es algo m�s complicado que eso. Intento explicarlo con un ejemplo m�s sencillo.
> 
> El siguiente cubo de datos:
> 
> genero	edad	profesi�n	provincia de residencia	Total personas
> 
> hombre	30	inform�tico	Madrid	100
> mujer	40	fontanero	Le�n	50
> hombre	40	inform�tico	Sevilla	800
> mujer	30	doctora	Zaragoza	60
> 
> Si filtras solo por provincia, te dar�a solo un resultado o si filtras por inform�tico dos, pero es por como son los datos del cubo.  Lo m�s habitual es que tengas que filtrar por cada una de las dimensiones para obtener un valor, pero depende de como sean los datos.
	
	