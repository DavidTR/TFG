#include "extern.h"


int Cluster_Mas_Cercano (double *patron)
/* Devuelve el cluster mas cercano al patron correspondiente calculando
   la distancia a todos los centros de clusters y devolviendo el que se
   encuentre a la minima distancia */

{
 double dist_minima, dist_act;
 int k, cluster;
 
 dist_minima=10E38;
 for (k=0;k<k_inicial;k++)
  {
   dist_act=Distancia (patron,Centros[k]);
   if (dist_act<dist_minima)
    {
     cluster=k;
     dist_minima=dist_act;
     }
   }
   
 return (cluster);
 }
       



char Seleccionado (int p, int n_centros)
/* Devuelve 1 si el patron de indice p en la tabla ha sido seleccionado
   como centro en el paso actual del algoritmo de inicializacion
   deterministica (en el que ya han sido determinados
   n_centros centros) y 0 en caso contrario */
   
{
 char sel;
 int k;
 
 sel=0; k=0;
 do
  {
   if (Seleccionados[k]==p)
    sel=1;
   else
    k++;
   }
 while (!sel && k<n_centros);
 
 return(sel);
 }
 


void Inicializa (void)
/* Inicializa la configuracion de clusters de partida. Si t_inic=0,
   la inicializacion es aleatoria mientras que si vale 1, se emplea
   el metodo deterministico de Kaufman */
   
{
 int i, j, k, k2, nuevo_centro;
 double dist_minima, dist_act, valor, max_ganancia;
 
 if (t_inic==0)
  {
   for (i=0;i<n_datos;i++)
    Conf[i]=Randint(0,k_inicial-1);
   }
   
 else
  if (t_inic==1)
   {
    /* Calculo la media del conjunto de ejemplos (la almaceno en
       Centros[1]), para buscar el patron mas centrico, elegirlo
       como primer centro y almacenarlo en Centros[0] */
    for (j=0; j<n_variables; j++)
     {
      Centros[1][j]=0.0;
      for (i=0; i<n_datos; i++)
       Centros[1][j] += tabla[i][j];
      }
    
    for (j=0; j<n_variables; j++)
     Centros[1][j] /= (double) n_datos;
    
    dist_minima=10E38;
    for (i=0; i<n_datos; i++)
     {
      dist_act=Distancia (tabla[i],Centros[1]);
      if (dist_act<dist_minima)
       {
        nuevo_centro=i;
        dist_minima=dist_act;
        }
      }
    
    for (j=0; j<n_variables; j++)
     Centros[0][j] = tabla[nuevo_centro][j];
    Seleccionados[0]=nuevo_centro;
    
    
    /* Ahora comienza el bucle greedy en el que, en cada paso, se
       escoge el patron mas alejado de los centros ya determinados
       y con mas ejemplos alrededor  como nuevo centro */
       
    for (k=1;k<k_inicial;k++)
     {
      for (i=0; i<n_datos; i++)
       if (Seleccionado(i,k-1))
        Ganancia[i]=-1.0;
       else
        {
         Ganancia[i]=0.0;
         for (j=0; j<n_datos; j++)
          if (!Seleccionado(j,k-1) && i!=j)
           {
            /* Se calcula la distancia del patron j al
               centro mas cercano de los ya determinados */
            dist_minima=10E38;
            for (k2=0;k2<k;k2++)
             {
              dist_act=Distancia (tabla[j],Centros[k2]);
              if (dist_act<dist_minima)
               dist_minima=dist_act;
              }
              
            valor=dist_minima - Distancia(tabla[j],tabla[i]);
            if (valor>0)
             Ganancia[i] += valor;
            }
         }
      
      max_ganancia=-1.0;
      for (i=0; i<n_datos; i++)
       if (!Seleccionado(i,k-1))
        {
         if (Ganancia[i]>max_ganancia)
          {
           nuevo_centro=i;
           max_ganancia=Ganancia[i];
           }
         }
      
      Seleccionados[k]=nuevo_centro;
      for (j=0; j<n_variables; j++)
       Centros[k][j] = tabla[nuevo_centro][j];
      }
    
    
    /* Una vez que se conocen los k centros, se genera una
       configuracion inicial asociando cada patron al cluster
       mas cercano */
    for (i=0; i<n_datos; i++)   
     Conf[i]=Cluster_Mas_Cercano (tabla[i]);
    }   
 }
 

void Calcula_Centros (void)
/* Calcula los centros de la configuracion de clusters
   actual */
   
{
 int i, j, k, n_elementos_cluster;
 
 for (k=0;k<k_inicial;k++)
  for (j=0;j<n_variables;j++)
   Centros[k][j]=0.0;
   
 k_actual = k_inicial;

 for (k=0;k<k_inicial;k++)
  {
   n_elementos_cluster=0;
   for (i=0;i<n_datos;i++)
    if (Conf[i]==k)
     {
      n_elementos_cluster++;
      for (j=0;j<n_variables;j++)
       Centros[k][j] += tabla[i][j];
      }
   
   if (n_elementos_cluster==0)
    {
     k_actual--;
     for (j=0;j<n_variables;j++)
      Centros[k][j] = 10E38;
     }
   else
    for (j=0;j<n_variables;j++)
     Centros[k][j] /= (double)n_elementos_cluster;
   }
   
   
 }
 
 
 
double Distancia (double *x, double *y)
/* Devuelve la distancia euclidea entre x e y */

{
 int i;
 double suma;
 
 suma = 0.0;
 for (i=0;i<n_variables;i++)
  suma += (x[i] - y[i]) * (x[i] - y[i]);
 //return (sqrt(suma));
 return (suma);
 }



double J (void)
/* Valida la configuracion de clusters actual calculando el indice J */

{
 int i;
 double suma;
 
 Calcula_Centros ();
 
 suma = 0.0;
 for (i=0;i<n_datos;i++)
  suma += Distancia (tabla[i],Centros[Conf[i]]);

 /* Aqui debajo tenemos cuatro posibilidades para distintos indices J.
    Para escoger uno, metemos los restantes entre comentarios: */

 /* El J normal con la distancia al cuadrado: */
 /*return (suma*suma);*/

 /* La media del J normal, dividiendo por el numero de ejemplos: */
 /*return (suma*suma/(double)n_datos);*/
  
 /* J sin la distancia al cuadrado: */
 return (suma);

 /* La media del J normal, dividiendo por el numero de ejemplos: */
 /*return (suma/(double)n_datos);*/
 
 }
