 /* PROGRAMA PRINCIPAL */

#include "global.h"
#include <time.h>

#define NUM_EJECUCIONES 1000


int main (int argc, char *argv[])

{
 char cambio;
 int ej, i, su_cluster;
 double media, max, min, J_act;

 if (argc!=2)
  {
   printf ("\nFormato: k-medias <nombre_fich_configuracion>\n\n");
   abort();
   }

 /* Lectura del fichero de configuracion e inicializacion de
    variables y estructuras */
 Input (argc,argv);

 /*printf ("\nPrograma en ejecucion ...\n\n");*/


	double tiempoInicial = clock();

 media=max=0.0; min=10E38;
 for (ej=0; ej<NUM_EJECUCIONES; ej++)
  {
   /* Generacion de la configuracion inicial */
   Inicializa ();

   /* Cuerpo principal del K-medias */
   do
    {
     cambio=0;
   
     Calcula_Centros ();
   
     for (i=0;i<n_datos;i++)
      {
       su_cluster=Cluster_Mas_Cercano (tabla[i]);
     
       if (su_cluster!=Conf[i])
        {
         cambio = 1;
         Conf[i] = su_cluster;
         }
       }
   
     }
   while (cambio);


   /* Se imprime el valor de J de la configuracion obtenida en la
      ejecucion actual*/
   J_act=J ();
   /*printf("Configuracion obtenida (%d): K= %d. J = %f.\n",ej,k_actual,J_act);*/
   
   /* Se actualizan los contadores */
   media += J_act;
   if (J_act<min)
    min=J_act;
   if (J_act>max)
    max=J_act;
   }
   
 /* Se imprimen la media, el maximo y el minimo J obtenido en las
    ejecuciones realizadas */
 media /= (double) NUM_EJECUCIONES;
 printf("\nResumen:\nMedia: %f. Maximo: %f. Minimo: %f\n\n",media,max,min);

 	//Dejo de mirar el tiempo aqui.
	double tiempoFinal = clock();
 
 /* Se liberan las estructuras de datos dinamicas */
  for (i=0;i<n_datos;i++)
  free (tabla[i]);
 free (tabla);

 free (Conf);
 
 for (i=0;i<k_inicial;i++)
  free (Centros[i]);
 free (Centros);
 
 free (Seleccionados);
 free (Ganancia);
	
 	printf("	-- Tiempo de ejecución: %f\n", (double)(tiempoFinal-tiempoInicial)/CLOCKS_PER_SEC);
 /*printf ("\n\n*****  FIN DEL PROGRAMA K-Medias *******\n\n");*/
 return (1);
 }

