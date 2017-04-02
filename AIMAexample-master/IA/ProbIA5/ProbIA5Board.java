package IA.ProbIA5;

import IA.Red.Centro;
import IA.Red.CentrosDatos;
import IA.Red.Sensor;
import IA.Red.Sensores;
//import pruebas.prueba.PairIndexDist;
//import pruebas.prueba.pairComparator;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by bejar on 17/01/17.
 */
public class ProbIA5Board {
    /* Class independent from AIMA classes
       - It has to implement the state of the problem and its operators
     *

    /* State data structure
1 "tablero" para todo el problema, donde tenemos las posiciones de cada nodo
    */
     static public CentrosDatos centrosDatos;
    static public Sensores sensores;
    private ArrayList<Tree> sol;
    //ArrayList  conex;
    static  Integer numCentros=1;
    static Integer numSensores=200;
    //CODIFICACION : si el Integer a >= numcentros es un sensor.
    //se busca en sensores a-numcentros 
    //else se busca en centros
    //static public ArrayList<ArrayList<Float> > distances;
    static public ArrayList<ArrayList<Integer> > closest;
    static public ArrayList<ArrayList<Float> > m_dist; //0-3 -> centros || 4-103 -> sensores
    
    static public ArrayList<Double> capacidades;//0-3 0(me va bien para calcular el volumen
    //lose demas son 1,2,5 en funcion de lo que sea

    
    public void insertsort(Integer i,Integer j, ArrayList<Integer> place){
        if(place.size()==0) place.add(j);
        else{
            int k=0;
            while (k < place.size() && m_dist.get(i).get(j) > m_dist.get(i).get(place.get(k))){
                k++;
            }
            place.add(k,j);
        }
    }
    
    public void calc_cercanos(){
        //Rellena closest con los nodos más cercanos para cada nodo
        closest=new ArrayList< ArrayList<Integer> >();
        for(int i=0;i<numCentros+numSensores;i++){
            closest.add(new ArrayList<Integer>());
            for(Integer j=0; j <numCentros+numSensores;j++){
                insertsort(i,j,closest.get(i));
            }
        }
        
        //comprobamos
        /*
        for (int i = 0; i < closest.size(); i++)
        {
            for (int j = 0; j < closest.get(0).size(); j++)
            {
                if (i <numCentros && j < numCentros) System.out.println ("closest de c"+i+": "+ closest.get(i).get(j));
                    else if (i < numCentros && j >= numCentros) System.out.println ("closest de c"+i+": "+closest.get(i).get(j));
                    else if (i >= numCentros && j < numCentros) System.out.println ("closest de s"+(i-numCentros)+": "+ closest.get(i).get(j));
                    else System.out.println ("closest de s"+(i-numCentros)+": "+ closest.get(i).get(j));
            }
            System.out.println();
        }
        */
        
    }
    
	class PairIndexDist{  // struct de la que esta formada la 2a matriz
		public int index;
		public Float dist;
	}
	
	class pairComparator implements Comparator<PairIndexDist> //comparador para el sort de la 2a matriz
	   {            
	        public int compare(PairIndexDist p1, PairIndexDist p2)
	        {
	            Float a1 = p1.dist;
	            Float a2 = p2.dist;
	            return a1.compareTo(a2);
	        }
	   }
	


	private ArrayList<Integer> hijos = new ArrayList<Integer>(numSensores+numCentros); //lista de hijos de cada nodo
	private ArrayList<ArrayList<PairIndexDist>> distanciesOrdenades = new ArrayList<ArrayList<PairIndexDist>>(); //2a matriz ordenada
   

    //VOLUME Y COST DEBEN ESTAR INICIALIZADOS Y A 0!!!
    public void volumeandcost(Integer volume, Float cost){
        Integer nvol=new Integer(0);
        Float ncost=new Float(0);
        for(Tree t:sol ){
            t.volumeandcost(nvol, ncost);
            volume= volume+nvol;
            ncost=ncost+ ncost;
            nvol=0; ncost=(float)0;
        }

    }
        public Float square(Float x){return x*x;}
        public void squaringdist(){
            for(ArrayList<Float> x : m_dist){
                for(Float f: x){
                    f=square(f);
                }
            }
        }

    public ArrayList<Integer> get30perc(int node){
        //WARNING ALGORITMO POCO EFICIENTE
        Integer size= new Integer(50*(numCentros+numSensores)/100);
        ArrayList<Integer> closest30= new ArrayList();
        //Cogemos el 30 % de primeros descartando el 1o(será el mismo)
        for(int i=1;i<size+1;i++){
            //closest30.add(new Integer(this.closest.get(i).get(node)));
            closest30.add(new Integer(this.closest.get(node).get(i)));
        }
        return closest30;
    }
    
    public ArrayList<Integer> getclosest(int node){
        return this.closest.get(node);
    }
    

    
    public int numNodos(){
        return numCentros+numSensores;
    }
    public int numCentros(){
        return numCentros;
    }
    

    
    
    public boolean is_goal(){
         // en principio no planteamos salirnos del 
         //espacio de soluciones en este problema 
         return true;
     }
 
    public ProbIA5Board(Integer ncenters,Integer nsensores){
        numCentros=ncenters;
       // distances=new ArrayList<ArrayList<Float> >(ncenters+nsensores);
        this.centrosDatos= new CentrosDatos(ncenters,1234);
        this.sensores= new Sensores(nsensores, 4321);
       // this.conex=new ArrayList();
       //for(int i= 0; i<numCentros ;i++) this.sol.add(new Tree(0));
        this.capacidades= new ArrayList();
       int i = 0;
       for(; i<numCentros;i++)capacidades.add(0.0);
       for(;i<numCentros+numSensores;i++){
           capacidades.add(sensores.get(i-numCentros).getCapacidad());
       }
       
       /*for (int j= 0; j < numCentros+numSensores; j++)
       {
           System.out.println("cap("+j+"): " +capacidades.get(j));
       }*/
       
    }
     // Some functions will be needed for creating a copy of the state
 
    public void setStart(){
        // solucion inicial INCOMPLETA
        //engancho todos los sensores al centro mas cercano libre
        //o bien al sensor más cercano libre
        Integer cand=new Integer(-1);
        for(int i=0;i<numSensores;i++){
            for(int j=0; j<numCentros;j++){
                if(!! sol.get(j).children().equals(25)){
                //candidato
                    if(cand.equals(-1))cand=sol.get(j).getId();
                }
            }
        }
    }
    
	private ArrayList<ArrayList<PairIndexDist>> Ordenar(ArrayList<ArrayList<Float>> distances){
		ArrayList<ArrayList<PairIndexDist>> distanciesOrdenades = new ArrayList<ArrayList<PairIndexDist>>(numSensores);
		for (int i = 0; i< numSensores+numCentros; ++i) {
			ArrayList<PairIndexDist> aux = new ArrayList<PairIndexDist>(numSensores+numCentros);
                        ArrayList<PairIndexDist> aux2 = new ArrayList<PairIndexDist>(numSensores+numCentros);//crea una fila de la matriz
			for (int j = 0; j < numCentros; ++j){
                                PairIndexDist P = new PairIndexDist();
				P.index = j;					  //la rellena con el indice original y la distancia de forma que los valores relevantes para definir un cable
				P.dist = distances.get(i).get(j); //son el indice de la fila y el valor contenido en el pair, llamado index.
                                aux.add(P);	
                        }
                   
                        for (int j = numCentros; j < numCentros+numSensores; ++j){
                                PairIndexDist P = new PairIndexDist();
				P.index = j;					  //la rellena con el indice original y la distancia de forma que los valores relevantes para definir un cable
				P.dist = distances.get(i).get(j); //son el indice de la fila y el valor contenido en el pair, llamado index.
				aux2.add(P);				
			}
                       
        
			Collections.sort(aux, new pairComparator()); 
                        Collections.sort(aux2, new pairComparator());//ordena cada fila segun el comparator d
                        aux.addAll(aux2);
			distanciesOrdenades.add(aux);  //finalmente a�ade cada fila a la matriz
		}
                
		return distanciesOrdenades;
	}
	
		private int NearFreeS2(int i1, int ns, int nc, ArrayList<Boolean> used, ArrayList<Integer> hijos) {
		for (int i= nc; i<nc+ns; ++i){
    		int i2 = distanciesOrdenades.get(i1).get(i).index;  // el for va de nc+1 (mas cercano) a nc+ns (mas lejano) comparando si estan libres
                System.out.println(i2+" used " + used.get(i2)+ " hijos " + hijos.get(i2));
    		if (used.get(i2) == false && hijos.get(i2)<2) return i2; // used == true implica que solo se unira a un nodo que ya se haya tratado y este formando parte de un arbol
		}
		return -1;
	}
        
        private int NearFreeS3(int i1, int ns, int nc, ArrayList<Boolean> used, ArrayList<Integer> hijos) {
		for (int i= nc; i<nc+ns; ++i){
    		int i2 = distanciesOrdenades.get(i1).get(i).index;  // el for va de nc+1 (mas cercano) a nc+ns (mas lejano) comparando si estan libres
                System.out.println(i2+" used " + used.get(i2)+ " hijos " + hijos.get(i2));
    		if (used.get(i2) == true && hijos.get(i2)<2) return i2; // used == true implica que solo se unira a un nodo que ya se haya tratado y este formando parte de un arbol
		}
		return -1;
	}
    
    private int NearFreeS(int i1, int ns, int nc, ArrayList<Integer> hijos) {
    	for (int i= nc; i<nc+ns; ++i){
    		int i2 = distanciesOrdenades.get(i1).get(i).index; // el for va de nc+1 (mas cercano) a nc+ns (mas lejano) comparando si estan libres
			if (i2 < i1 && hijos.get(i2)<2) return i2; //si esta por debajo (ya esta unido al arbol) y libre se le une
		}
		return -1;
	}

	private int NearFreeC(int i1, int nc, ArrayList<Integer> hijos) {
		for (int i= 0; i < nc; ++i){ 	// el for va de 0 (mas cercano) a nc (mas lejano) comparando si estan libres
			int i2 = distanciesOrdenades.get(i1).get(i).index;
			if (hijos.get(i2)<25) return i2; //si esta libre se le une
		}
		return -1;
	}
	
	
    public void init1(){
    	int nc = numCentros;
    	int ns = numSensores;
        sol = new ArrayList<Tree> (); //creamos un array 
    	for (int i =0; i< nc; ++i) sol.add(new Tree(i));
    	distanciesOrdenades = Ordenar(m_dist);  


	for (int i = 0; i < nc+ns;++i) hijos.add(0);
       	boolean saturados = false;
    	for(int i=nc;i<nc+ns;i++){
    		int n = 0;
    		if (! saturados) {
                    	n = NearFreeC(i,nc,hijos);  //-1 si no encuentra centros libres: centros saturados
    			saturados = (n == -1);
    		}
            if (saturados) n = NearFreeS(i,ns,nc,hijos); //solo debe buscar sensores por debajo de i
            //System.out.println(i+ " es hijo de: "+n);
            if(!saturados) sol.get(n).add(new Tree(i));
            else for (int j = 0; j< sol.size();++j){
               //int m = 0;
                if(i==195){
                 //   System.out.println("nodo que inserto "+  i+  "nodo donde inserto" + n);
                  //  System.out.println("dde busco"+ j);
                }
                
                Tree t = sol.get(j).find(n,null);
                //System.out.println("b = "+ m);
                if (t!=null){
                    t.add(new Tree(i));
                 //   System.out.println("HOLA" + t.getId());
                    break;
                }	
            }
            hijos.set(n, hijos.get(n)+1);
        }
        printsol();
    }
    
    //COlocamos sensores x orden en el centro hasta rellenarlo, el resto de nodos se enganchan a los otros sensores
    public void init2(){
        
    	int nc = numCentros;
    	int ns = numSensores;
        
        /*System.out.println ("CAPACIDADES:");
        for (int x = 0; x <nc+ns; x++)
            System.out.println("cap"+x+": "+capacidades.get(x));*/
        
        sol = new ArrayList<Tree> (); //creamos un array 
        int i = 0;
    	for (; i< nc; ++i) sol.add(new Tree(i)); //añadimos los centros


        //Añadimos 25 sensores a cada centro
        
        for (int k = 0; k < nc; k++)
        {
            int tope = ns;
            if (ns > 25) tope = 25; 
            for (int q = 0; q <tope; q++)
            {
                sol.get(k).add(new Tree(i));
                //System.out.println("añadiendo sensor "+(i-nc)+ " al centro "+ k);
                i++;
            }
        }
        while (i < nc+ns) 
        {
            //System.out.println("This isn't even my final form");
           
            for (int j = 0; j <nc ;j++){  //centros
                boolean b = false;
                boolean centro = true;
                
                //System.out.println("j: "+j);
                //System.out.println("nc+ns: "+(nc+ns));
                Tree search = sol.get(0);
                for (int l = 0; l < sol.get(j).children() && !b; l++){ //hijos
                    //System.out.println("--i: "+i+ "; search: "+search.getId());
                    b = (sol.get(j).putSensor(search,i,centro));
                    if (b){
                        //System.out.println("--i: "+i+ "; b:"+b);
                        i++;
                        search = sol.get(j);
                        //System.out.println("--next i: "+i);
                    }
                    else {
                        //System.out.println("ELSEEEEEEEEEEEEEEEEEEEEEEEE");
                        search = sol.get(j).child(l);
                        centro = true;
                    }
                }
                //if (!b) System.out.println("****************************ESTOO SE VA A DESCONTROLAAAAAAR!!!!!");
                //printsol();
            }
            
            
        }
        //printsol();
    }
    
    
    
    public void printsol(){
        for(Tree t :sol)t.print();
    }
    
    public void init3(){ //roundrobin de centros para escoger su mas cercano + sensor mas cercano cuando se saturan

	distanciesOrdenades = Ordenar(m_dist);
        int nc = numCentros;
    	int ns = numSensores;
        sol = new ArrayList<Tree> (); 
    	for (int i =0; i< nc; ++i) sol.add(new Tree(i));
        for (int i = 0; i < nc+ns;++i) hijos.add(0);
     	ArrayList<Boolean> used = new ArrayList<Boolean>();
        for (int i = 0; i < nc;++i)used.add(true);
        for (int i = 0; i < ns;++i)used.add(false);
    	Boolean done = false;
    	for(int i=0;i<25;++i){
    		for (int j = 0; j < nc; ++j){
    			int n = NearFreeS2(i,ns,nc,used,hijos);
    			done = (n == -1); 
    			if (done) break;
    			used.set(n,true);
    			sol.get(j).add(new Tree(n));
    			hijos.set(j, hijos.get(j)+1);
    		}
    		if (done) break;
    	}
    	int i = used.indexOf(false);
    	while ( i != -1 ){
    		int n = NearFreeS3(i,ns, nc,used,hijos);
                for (int j = 0; j < sol.size(); ++j){
                    Tree t = sol.get(j).find(n,null);
                    if (t!=null){
                        t.add(new Tree(i));
                        used.set(i, true);
                    }
                }
                System.out.println(n);
                hijos.set(n, hijos.get(n)+1);
                i = used.indexOf(false);
    	}
    	printsol();
    }
   
    


	

    
    public void setSol(ArrayList<Tree  > a){
        this.sol= a;
    }
    
    public double distance(int a, int sensor){
        if(numCentros > a && numCentros > sensor){ return Double.MAX_VALUE; }//dos centros
        else if(numCentros <= a && numCentros > sensor){//a es sensor,b es centro
            Centro s1= centrosDatos.get(sensor);
            Sensor s2=sensores.get(a-numCentros);
            return sqrt(pow(s1.getCoordX()-s2.getCoordX(),2) +pow(s1.getCoordY()-s2.getCoordY(),2));
        }
        else if(numCentros <= a && numCentros <= sensor){//a es un sensor b es sensor
            Sensor s1=sensores.get(a-numCentros);
            Sensor s2=sensores.get(sensor-numCentros);
            return sqrt(pow(s1.getCoordX()-s2.getCoordX(),2) +pow(s1.getCoordY()-s2.getCoordY(),2));
        }else {// a es centro   b es sensor
            Centro s1= centrosDatos.get(a);
            Sensor s2=sensores.get(sensor-numCentros);
            return sqrt(pow(s1.getCoordX()-s2.getCoordX(),2) +pow(s1.getCoordY()-s2.getCoordY(),2));
        }
    }
    
 
    public double heuristic(){
        // compute the number of coins out of place respect to solution
        double sum =0;
        
        System.out.println("------heuristic-------");
        //falta implementar cada uno de los heuristicos, uno que funcione
        //con suma, otro con una división, y quizás uno que los mezcle
        Float cost = new Float(0);
        Pair p= new Pair();
        Integer v_util = new Integer(0),vtotal= new Integer(0);
        for (int i = 0; i< sol.size(); i++)
        {
            //System.out.println("preecost");
            //sol.get(0).print();
            
            p=sol.get(i).volumeandcostp();
            
            System.out.println("----------------> arbol "+i+ ": *volumen: " +p.getVol()+", *coste: "+ p.getCost());
            sol.get(0).print();
          
            
            vtotal=vtotal+p.getVol();
            cost=cost+p.getCost();
        }
        //sum = heuristic2(vtotal,cost); 
        
        sum = heuristic3(vtotal,cost); 
        System.out.println("suma total:" +sum);
        System.out.println("*********************************");

        return sum;
    }
    
    public double heuristic2(Integer v_util, Float cost) { //coste
        Float k = new Float(3); // constante experimental
        System.out.println("\n heuristic value:" +cost+"/"+k * pow(v_util,3));
        Double h2 = cost /(k* pow(v_util,3) ) ;
        return h2;
    }
   
   
    //
    public double heuristic3(Integer v_util, Float cost){
        Integer k = 50; //constante experimental
        double h4 = cost- k*pow(v_util,3) ;
        return h4;
    }
    
    public ProbIA5Board copyestat(){
        ProbIA5Board a = new ProbIA5Board(numCentros,sensores.size());
        ArrayList<Tree> dif =new ArrayList();
       for(Tree  t : sol )dif.add(t.copy());
        a.setSol(dif);
        return a;
    }
    
    public Integer father(Integer fill){
        //System.out.println("--------------FATHER-GOD----------: " + fill);
        Integer a=null;
        for(Tree t:sol){
            //System.out.println("bucle for-> soy "+t.getId());
             a=t.father(t,fill);
             //System.out.println("fii father: "+ a);
             if(a!=null)break;
        }
        return a;
    }
    
    public boolean change(Integer pare,Integer noupare, Integer fill){
        
        //System.out.println ("----------> change ("+fill+" -> "+ pare+" to "+fill+" -> "+ noupare+").");
        if(fill.equals(noupare) || noupare.equals(pare)) {
            //System.out.println("SPARTAAAA!!!!!!!");
            return false;
        }
        
        /*System.out.println ("------> print antes1");
        sol.get(0).print();
        System.out.println ("------> print antes2");
        
        System.out.println ("noupare: "+noupare);
        System.out.println ("pare: "+ pare);*/
        Tree father= null;
        Tree newfather = null;
        int i =0;
        //System.out.println ( "noupare " +noupare + " fill " + fill);
        while(newfather==null && i <sol.size()){
            //System.out.println ("chivato1");
            //System.out.println("i: "+i);
            newfather=sol.get(i).find(noupare, fill);
            i++;
        }
        if (newfather != null) {
            //System.out.println("newfather: "+ newfather.getId());
            i=0;
            if(fill==noupare || fill == pare) System.out.println("ERROR FILL == PARES");
            if((noupare >= numCentros && newfather.children().equals(2)) ||
                    (noupare < numCentros && newfather.children().equals(25))){
                //System.out.println("DOBLEEEEE IFFFFFFFFF");
                return false;
            }
            //System.out.println("GOD FATHER: " + father);
            while(newfather != null && father==null){
                //System.out.println ("chivato2");
                //System.out.println ("i " + i + " pare " +pare);
                /*if (i ==2) {
                    System.out.println("***********");
                    sol.get(2).print();
                }*/
                father=sol.get(i).find(pare, fill);

                i++;
            }
        
            //if (father.getId() == fill || newfather.getId() == fill) System.out.println("ERROR!!!!!!!!!!");
            //System.out.println("GOD FATHER2: " + father.getId());
            newfather.add(father.quickfind(fill));//ho trobara a al primera iteracio 
            //aixi que es prou eficient
            father.remove(fill);//aixo tambe es directe
            /*System.out.println ("------> print despues true");
            sol.get(0).print();
            System.out.println ("------> print despues true2");*/
            return true;
        }
        else {
            /*System.out.println ("------> print despues false");
            sol.get(0).print();
            System.out.println ("------> print despues false2");*/
            return false;
        }
    }
     

public void calc_dist()
    {
        int n_c = centrosDatos.size();
        System.out.println("n_c¨: " + n_c);
        int n = n_c + sensores.size();
        System.out.println("n¨: " + n);
        m_dist= new ArrayList<ArrayList<Float>>(n);
        float x_var = 0.0f;
        float y_var = 0.0f;
        for (int i = 0; i < n; i++)
        {
            m_dist.add(new ArrayList<Float>());
            for (int j = 0; j < n; j++)
            {
                //System.out.println("i¨: " + i + "; j : " + j );
                if (i < n_c && j < n_c){
                    //System.out.println("if");
                    x_var = (float)centrosDatos.get(i).getCoordX() - (float)centrosDatos.get(j).getCoordX();
                    y_var = (float)centrosDatos.get(i).getCoordY() - (float)centrosDatos.get(j).getCoordY();
                    
                }
                else if (i < n_c && j >= n_c){
                    //System.out.println("else if1");
                    x_var = (float)centrosDatos.get(i).getCoordX() - (float)sensores.get(j-n_c).getCoordX();
                    y_var = (float)centrosDatos.get(i).getCoordY() - (float)sensores.get(j-n_c).getCoordY();
                    
                }
                else if (i >= n_c && j < n_c){
                    //System.out.println("else if2");
                    x_var = (float)sensores.get(i-n_c).getCoordX() - (float)centrosDatos.get(j).getCoordX();
                    y_var = (float)sensores.get(i-n_c).getCoordY() - (float)centrosDatos.get(j).getCoordY();
                }
                else{
                    //System.out.println("else");
                    x_var = (float)sensores.get(i-n_c).getCoordX() - (float)sensores.get(j-n_c).getCoordX();
                    y_var = (float)sensores.get(i-n_c).getCoordY() - (float)sensores.get(j-n_c).getCoordY();
                }
                x_var = (float) x_var*x_var;
                y_var = (float) y_var*y_var;
                
                Float dist = new Float(sqrt(x_var + y_var));
                
                m_dist.get(i).add(dist*dist);
                //m_dist.get(i).add(dist);
            }
        }
        //Comprobamos
        /*for(int i=0;i<numCentros+numSensores;i++){
            for(int j=0;j<numCentros+numSensores;j++)
                {
                    if (i <n_c && j < n_c) System.out.println ("distancia de c"+i+" a c"+j+" : "+ m_dist.get(i).get(j));
                    else if (i < n_c && j >= n_c) System.out.println ("distancia de c"+i+" a s"+(j-n_c)+" : "+ m_dist.get(i).get(j));
                    else if (i >= n_c && j < n_c) System.out.println ("distancia de s"+(i-n_c)+" a c"+j+" : "+ m_dist.get(i).get(j));
                    else System.out.println ("distancia de s"+(i-n_c)+" a s"+(j-n_c)+" : "+ m_dist.get(i).get(j));
                }
            System.out.println();
        }*/
    }
    
    /**
        /////////////////////
    public double inorden_cost(Tree t)
    {
        double cost = 0;
        if (t == null)
            return 0;
        else
        {
            int id = t.getId();
            cost = get_cost(t.child(0),id);
            for (int i = 0; i < t.children(); i++)
                cost += get_cost(t.child(i),id);
        }
        return cost;
    }
    
    public double get_cost (Tree t, int id)
    {
        double cost = 0;
        if (t== null)
            return 0;
        else 
        {
            int n_id = t.getId();
            cost = get_cost(t.child(0),id);
            float d = m_dist[n_id][id]*m_dist[n_id][id];
            double v = sensores.get(n_id).getCapacidad();
            cost += d*v;
            for (int i = 0; i < t.children(); i++)
                cost += get_cost(t.child(i),id);
        }
        return cost;
    }
    
    public double inorden_v_tree(Tree t)
    {
        double v = 0;
        if (t == null)
            return 0;
        else 
        {
            //int id = t.getId();
            v = get_v(t.child(0), new Double (0.0));
            for (int i = 0; i < t.children(); i++)
                v += get_v(t.child(i),0.0);
                
        }
        return v;
        
    }
    public double get_v (Tree t, Double my_v)
    {
        double v = 0;
        
        if (t == null)
            return 0.0;
        else 
        {
            Double c_v =new Double (0.0);
            int n_id = t.getId();
            v = get_v (t.child(0),c_v);
            double v2 = sensores.get(n_id).getCapacidad();
            
            for (int i = 0; i <t.children(); i++)
            {
                v += get_v(t.child(i), 0.0);
            }
        
        }
        return v;
    }
    //////////////
    
    
    
    
    
    **/

public void printdist(){
        System.out.println("dist");
        for(int i = 0; i<m_dist.size() ; i++){
            System.out.println(i + " :" );
            for(int j = 0; j<m_dist.size();j++){
                System.out.print(m_dist.get(i).get(j) +" ");
            }
            System.out.println();
        }
    }
    public void printclose(){
        System.out.println("close");
        for(int i = 0; i<m_dist.size() ; i++){
            System.out.println(i + " :" );
            for(int j = 0; j<m_dist.size();j++){
                System.out.print(closest.get(i).get(j) +" ");
            }
            System.out.println();
        }
    }
}

//PRINT de INIT2 para: 2 centros/200 sensores
    /*
Nodo 0(size: 25)  hijos:
2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 
Nodo 2(size: 2)  hijos:
52 53 
Nodo 52(size: 2)  hijos:
102 104 
Nodo 102(size: 0)  hijos:

Nodo 104(size: 0)  hijos:

Nodo 53(size: 2)  hijos:
106 108 
Nodo 106(size: 0)  hijos:

Nodo 108(size: 0)  hijos:

Nodo 3(size: 2)  hijos:
54 55 
Nodo 54(size: 2)  hijos:
110 112 
Nodo 110(size: 0)  hijos:

Nodo 112(size: 0)  hijos:

Nodo 55(size: 2)  hijos:
114 116 
Nodo 114(size: 0)  hijos:

Nodo 116(size: 0)  hijos:

Nodo 4(size: 2)  hijos:
56 57 
Nodo 56(size: 2)  hijos:
118 120 
Nodo 118(size: 0)  hijos:

Nodo 120(size: 0)  hijos:

Nodo 57(size: 2)  hijos:
122 124 
Nodo 122(size: 0)  hijos:

Nodo 124(size: 0)  hijos:

Nodo 5(size: 2)  hijos:
58 59 
Nodo 58(size: 2)  hijos:
126 128 
Nodo 126(size: 0)  hijos:

Nodo 128(size: 0)  hijos:

Nodo 59(size: 2)  hijos:
130 132 
Nodo 130(size: 0)  hijos:

Nodo 132(size: 0)  hijos:

Nodo 6(size: 2)  hijos:
60 61 
Nodo 60(size: 2)  hijos:
134 136 
Nodo 134(size: 0)  hijos:

Nodo 136(size: 0)  hijos:

Nodo 61(size: 2)  hijos:
138 140 
Nodo 138(size: 0)  hijos:

Nodo 140(size: 0)  hijos:

Nodo 7(size: 2)  hijos:
62 63 
Nodo 62(size: 2)  hijos:
142 144 
Nodo 142(size: 0)  hijos:

Nodo 144(size: 0)  hijos:

Nodo 63(size: 2)  hijos:
146 148 
Nodo 146(size: 0)  hijos:

Nodo 148(size: 0)  hijos:

Nodo 8(size: 2)  hijos:
64 65 
Nodo 64(size: 2)  hijos:
150 152 
Nodo 150(size: 0)  hijos:

Nodo 152(size: 0)  hijos:

Nodo 65(size: 2)  hijos:
154 156 
Nodo 154(size: 0)  hijos:

Nodo 156(size: 0)  hijos:

Nodo 9(size: 2)  hijos:
66 67 
Nodo 66(size: 2)  hijos:
158 160 
Nodo 158(size: 0)  hijos:

Nodo 160(size: 0)  hijos:

Nodo 67(size: 2)  hijos:
162 164 
Nodo 162(size: 0)  hijos:

Nodo 164(size: 0)  hijos:

Nodo 10(size: 2)  hijos:
68 69 
Nodo 68(size: 2)  hijos:
166 168 
Nodo 166(size: 0)  hijos:

Nodo 168(size: 0)  hijos:

Nodo 69(size: 2)  hijos:
170 172 
Nodo 170(size: 0)  hijos:

Nodo 172(size: 0)  hijos:

Nodo 11(size: 2)  hijos:
70 71 
Nodo 70(size: 2)  hijos:
174 176 
Nodo 174(size: 0)  hijos:

Nodo 176(size: 0)  hijos:

Nodo 71(size: 2)  hijos:
178 180 
Nodo 178(size: 0)  hijos:

Nodo 180(size: 0)  hijos:

Nodo 12(size: 2)  hijos:
72 73 
Nodo 72(size: 2)  hijos:
182 184 
Nodo 182(size: 0)  hijos:

Nodo 184(size: 0)  hijos:

Nodo 73(size: 2)  hijos:
186 188 
Nodo 186(size: 0)  hijos:

Nodo 188(size: 0)  hijos:

Nodo 13(size: 2)  hijos:
74 75 
Nodo 74(size: 2)  hijos:
190 192 
Nodo 190(size: 0)  hijos:

Nodo 192(size: 0)  hijos:

Nodo 75(size: 2)  hijos:
194 196 
Nodo 194(size: 0)  hijos:

Nodo 196(size: 0)  hijos:

Nodo 14(size: 2)  hijos:
76 77 
Nodo 76(size: 2)  hijos:
198 200 
Nodo 198(size: 0)  hijos:

Nodo 200(size: 0)  hijos:

Nodo 77(size: 0)  hijos:

Nodo 15(size: 2)  hijos:
78 79 
Nodo 78(size: 0)  hijos:

Nodo 79(size: 0)  hijos:

Nodo 16(size: 2)  hijos:
80 81 
Nodo 80(size: 0)  hijos:

Nodo 81(size: 0)  hijos:

Nodo 17(size: 2)  hijos:
82 83 
Nodo 82(size: 0)  hijos:

Nodo 83(size: 0)  hijos:

Nodo 18(size: 2)  hijos:
84 85 
Nodo 84(size: 0)  hijos:

Nodo 85(size: 0)  hijos:

Nodo 19(size: 2)  hijos:
86 87 
Nodo 86(size: 0)  hijos:

Nodo 87(size: 0)  hijos:

Nodo 20(size: 2)  hijos:
88 89 
Nodo 88(size: 0)  hijos:

Nodo 89(size: 0)  hijos:

Nodo 21(size: 2)  hijos:
90 91 
Nodo 90(size: 0)  hijos:

Nodo 91(size: 0)  hijos:

Nodo 22(size: 2)  hijos:
92 93 
Nodo 92(size: 0)  hijos:

Nodo 93(size: 0)  hijos:

Nodo 23(size: 2)  hijos:
94 95 
Nodo 94(size: 0)  hijos:

Nodo 95(size: 0)  hijos:

Nodo 24(size: 2)  hijos:
96 97 
Nodo 96(size: 0)  hijos:

Nodo 97(size: 0)  hijos:

Nodo 25(size: 2)  hijos:
98 99 
Nodo 98(size: 0)  hijos:

Nodo 99(size: 0)  hijos:

Nodo 26(size: 2)  hijos:
100 101 
Nodo 100(size: 0)  hijos:

Nodo 101(size: 0)  hijos:

Nodo 1(size: 25)  hijos:
27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50 51 
Nodo 27(size: 2)  hijos:
103 105 
Nodo 103(size: 2)  hijos:
107 109 
Nodo 107(size: 0)  hijos:

Nodo 109(size: 0)  hijos:

Nodo 105(size: 2)  hijos:
111 113 
Nodo 111(size: 0)  hijos:

Nodo 113(size: 0)  hijos:

Nodo 28(size: 2)  hijos:
115 117 
Nodo 115(size: 2)  hijos:
119 121 
Nodo 119(size: 0)  hijos:

Nodo 121(size: 0)  hijos:

Nodo 117(size: 2)  hijos:
123 125 
Nodo 123(size: 0)  hijos:

Nodo 125(size: 0)  hijos:

Nodo 29(size: 2)  hijos:
127 129 
Nodo 127(size: 2)  hijos:
131 133 
Nodo 131(size: 0)  hijos:

Nodo 133(size: 0)  hijos:

Nodo 129(size: 2)  hijos:
135 137 
Nodo 135(size: 0)  hijos:

Nodo 137(size: 0)  hijos:

Nodo 30(size: 2)  hijos:
139 141 
Nodo 139(size: 2)  hijos:
143 145 
Nodo 143(size: 0)  hijos:

Nodo 145(size: 0)  hijos:

Nodo 141(size: 2)  hijos:
147 149 
Nodo 147(size: 0)  hijos:

Nodo 149(size: 0)  hijos:

Nodo 31(size: 2)  hijos:
151 153 
Nodo 151(size: 2)  hijos:
155 157 
Nodo 155(size: 0)  hijos:

Nodo 157(size: 0)  hijos:

Nodo 153(size: 2)  hijos:
159 161 
Nodo 159(size: 0)  hijos:

Nodo 161(size: 0)  hijos:

Nodo 32(size: 2)  hijos:
163 165 
Nodo 163(size: 2)  hijos:
167 169 
Nodo 167(size: 0)  hijos:

Nodo 169(size: 0)  hijos:

Nodo 165(size: 2)  hijos:
171 173 
Nodo 171(size: 0)  hijos:

Nodo 173(size: 0)  hijos:

Nodo 33(size: 2)  hijos:
175 177 
Nodo 175(size: 2)  hijos:
179 181 
Nodo 179(size: 0)  hijos:

Nodo 181(size: 0)  hijos:

Nodo 177(size: 2)  hijos:
183 185 
Nodo 183(size: 0)  hijos:

Nodo 185(size: 0)  hijos:

Nodo 34(size: 2)  hijos:
187 189 
Nodo 187(size: 2)  hijos:
191 193 
Nodo 191(size: 0)  hijos:

Nodo 193(size: 0)  hijos:

Nodo 189(size: 2)  hijos:
195 197 
Nodo 195(size: 0)  hijos:

Nodo 197(size: 0)  hijos:

Nodo 35(size: 2)  hijos:
199 201 
Nodo 199(size: 0)  hijos:

Nodo 201(size: 0)  hijos:

Nodo 36(size: 0)  hijos:

Nodo 37(size: 0)  hijos:

Nodo 38(size: 0)  hijos:

Nodo 39(size: 0)  hijos:

Nodo 40(size: 0)  hijos:

Nodo 41(size: 0)  hijos:

Nodo 42(size: 0)  hijos:

Nodo 43(size: 0)  hijos:

Nodo 44(size: 0)  hijos:

Nodo 45(size: 0)  hijos:

Nodo 46(size: 0)  hijos:

Nodo 47(size: 0)  hijos:

Nodo 48(size: 0)  hijos:

Nodo 49(size: 0)  hijos:

Nodo 50(size: 0)  hijos:

Nodo 51(size: 0)  hijos:
*/