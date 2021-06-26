package it.polito.tdp.PremierLeague.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.PremierLeague.db.PremierLeagueDAO;



public class Model {
	
	PremierLeagueDAO dao;
	private SimpleDirectedWeightedGraph<Team, DefaultWeightedEdge> grafo;
	HashMap<Integer, Team> idMap;
	public Model() {
		 dao= new PremierLeagueDAO();
	}
	public String creaGrafo() {
		grafo = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		idMap= new HashMap<Integer, Team>();
		dao.getVertices(idMap);
		Graphs.addAllVertices(grafo, idMap.values());
		String s="Vertici: "+(grafo.vertexSet().size());
		dao.getPunteggi(idMap);
		for(Team t1: idMap.values())
		{
			for(Team t2: idMap.values())
			{
				if(t1.punteggio>t2.punteggio)
					if(grafo.containsVertex(t1)&&grafo.containsVertex(t2)&&!grafo.containsEdge(t1, t2))
					{
						Graphs.addEdge(grafo,t1, t2, t1.punteggio-t2.punteggio);
					}
				if(t1.punteggio<t2.punteggio)
					if(grafo.containsVertex(t1)&&grafo.containsVertex(t2)&&!grafo.containsEdge(t2, t1))
					{
						Graphs.addEdge(grafo,t2, t1, t2.punteggio-t1.punteggio);
					}
			}
		}
		s+=" Archi:"+(grafo.edgeSet().size());
		return s;
	}
	public Set<Team> getSquadre(){
		return grafo.vertexSet();
	}
	LinkedList<Team> migliori=new LinkedList<>();
	LinkedList<Team> peggiori=new LinkedList<>();
	public String classifica(Team t)
	{
		String s="\nSquadre vinte: \n";
		migliori.clear();
		peggiori.clear();
		
		for(Team t1: Graphs.successorListOf(grafo, t))
		{
			s+= t1.name+ " "+ t1.punteggio+"\n";
			peggiori.add(t1);
		}
		s+= "\nSquadre vincitrici: \n";
		for(Team t1: Graphs.predecessorListOf(grafo, t))
		{
			migliori.add(t1);
			s+= t1.name+ " "+ t1.punteggio+"\n";
		}
		return s;
	}
	
	int nreporter;
	int xsoglia;
	public String init(int n, int x){
		nreporter=n;
		xsoglia=x;
		LinkedList<Match> partite=new LinkedList<Match>(dao.listAllMatches());
		for(Team t: grafo.vertexSet())
		{
			t.setReporters(n);
		}
		return simulator(partite);
	}
	
	public String simulator(LinkedList<Match> partite) {
		int sum=0;
		int critiche=0;
		for(Match m:partite)
		{
			
			Random rand = new Random();
			int prob = rand. nextInt(100)+1;
			Team vincente= idMap.get(m.teamHomeID);
			Team perdente= idMap.get(m.teamAwayID);
			if(m.getReaultOfTeamHome()==1)
			{
				vincente= idMap.get(m.teamHomeID);
				perdente= idMap.get(m.teamAwayID);
			}
			if(m.getReaultOfTeamHome()==-1)
			{
				vincente= idMap.get(m.teamAwayID);
				perdente= idMap.get(m.teamHomeID);
			}
			sum+=vincente.getReporters()+perdente.getReporters();
			if((vincente.getReporters()+perdente.getReporters())<xsoglia)
				critiche++;
			if(prob<=50)
			{
				classifica(vincente);
				System.out.println(migliori.size());
				if(migliori.size()!=0&&vincente.getReporters()!=0)
				{
					int prob1 = rand.nextInt(migliori.size());
					migliori.get(prob1).setReporters(migliori.get(prob1).getReporters()+1);
					vincente.setReporters(vincente.getReporters()-1);
				}
			}
			
			if(prob<=20)
			{
				classifica(perdente);
				System.out.println(peggiori.size());
				if(peggiori.size()!=0&&perdente.getReporters()!=0)
				{
					int prob1 = rand.nextInt(peggiori.size());
					int darimuovere = rand.nextInt(perdente.getReporters())+1;
					peggiori.get(prob1).setReporters(peggiori.get(prob1).getReporters()+darimuovere);
					perdente.setReporters(perdente.getReporters()-darimuovere);
				}
			}
			
		}
		String s="\n Media reporters: "+(double)sum/(double)partite.size()+ " Critiche: "+critiche;
		return s;
		
	}
}
