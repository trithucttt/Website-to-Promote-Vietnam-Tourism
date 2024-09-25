package com.trithuc.config;

import java.io.IOException;
import java.util.Optional;

import com.trithuc.model.User;
import com.trithuc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JWTRequesFilter extends OncePerRequestFilter{

	
	@Autowired
	private JWTTokenUtil jwtTokenUtil;
	
	@Autowired
	private UserRepository userRepository;
	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		final String requesTokenHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (requesTokenHeader == null || requesTokenHeader.isBlank() || requesTokenHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}
		String jwtToken = requesTokenHeader.split(" ")[1].trim();
		String usernam = jwtTokenUtil.getUsernameFromToken(jwtToken);
		//UserDetails userValObject =  userDetailsService.loadUserByUsername(usernam);
		Optional<User> user = Optional.ofNullable(userRepository.findByUsername(usernam).orElse(null));
		
		if (jwtTokenUtil.validateToken(jwtToken, user.get())) {
		    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(user, null);
		    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
		} else {
		    SecurityContextHolder.clearContext();
		}
		filterChain.doFilter(request, response);
	}

	
}
