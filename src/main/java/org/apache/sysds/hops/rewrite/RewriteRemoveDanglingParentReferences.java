/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sysds.hops.rewrite;

import java.util.ArrayList;
import java.util.List;

import org.apache.sysds.common.Types.OpOp1;
import org.apache.sysds.common.Types.OpOpData;
import org.apache.sysds.common.Types.OpOpN;
import org.apache.sysds.hops.DataOp;
import org.apache.sysds.hops.FunctionOp;
import org.apache.sysds.hops.Hop;
import org.apache.sysds.hops.NaryOp;
import org.apache.sysds.hops.UnaryOp;

/**
 * This rewrite is a general-purpose cleanup pass that removes any
 * dangling parent references in one pass through the hop DAG. These
 * dangling references could have been introduced by rewrites that 
 * remove operators but miss a proper cleanup. 
 * 
 */
public class RewriteRemoveDanglingParentReferences extends HopRewriteRule
{
	@Override
	public ArrayList<Hop> rewriteHopDAGs(ArrayList<Hop> roots, ProgramRewriteStatus state) {
		if( roots == null )
			return null;
		
		int numRm = 0;
		for( Hop h : roots ) 
			numRm += removeDanglingParentReferences( h, false );
		
		if( LOG.isDebugEnabled() && numRm > 0 )
			LOG.debug("Remove Dangling Parents - removed "+numRm+" operators.");
		
		return roots;
	}

	@Override
	public Hop rewriteHopDAG(Hop root, ProgramRewriteStatus state) {
		if( root == null )
			return root;
		
		//note: the predicate can have an arbitrary root node
		//and hence, we pin this node to prevent its removal
		int numRm = removeDanglingParentReferences( root, true );
		
		if( LOG.isDebugEnabled() && numRm > 0 )
			LOG.debug("Remove Dangling Parents - removed "+numRm+" operators.");
		
		return root;
	}
	
	private int removeDanglingParentReferences( Hop hop, boolean pin ) {
		//check mark processed
		if( hop.isVisited() )
			return 0;
		
		//mark node itself as processed (because it's reachable over parents)
		hop.setVisited();
		
		//process parents recursively (w/ potential modification)
		int count = 0;
		for( int i=0; i<hop.getParent().size(); i++ ) {
			Hop p = hop.getParent().get(i);
			count += removeDanglingParentReferences(p, false);
			i -= hop.getParent().contains(p) ? 0 : 1; //skip back
		}
		
		//process node itself and children recursively
		List<Hop> inputs = hop.getInput();
		if( !pin && hop.getParent().isEmpty() && !isValidRootNode(hop) ) {
			HopRewriteUtils.cleanupUnreferenced(hop);
			count++;
		}
		for( int i=0; i<inputs.size(); i++ )
			count += removeDanglingParentReferences(inputs.get(i), false);
		
		return count;
	}
	
	private static boolean isValidRootNode(Hop hop) {
		return (hop instanceof DataOp && ((DataOp)hop).isWrite())
			|| (hop instanceof UnaryOp && ((UnaryOp)hop).getOp()==OpOp1.STOP)
			|| (hop instanceof UnaryOp && ((UnaryOp)hop).getOp()==OpOp1.PRINT)
			|| (hop instanceof UnaryOp && ((UnaryOp)hop).getOp()==OpOp1.ASSERT)
			|| (hop instanceof NaryOp && ((NaryOp)hop).getOp()==OpOpN.PRINTF)
			|| (hop instanceof FunctionOp)
			|| (hop instanceof DataOp && ((DataOp)hop).getOp()==OpOpData.FUNCTIONOUTPUT);
	}
}
